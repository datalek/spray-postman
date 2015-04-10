package wannaup.services

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson._
import reactivemongo.api._
import wannaup.db.Mongo
import wannaup.models._
import wannaup.util.Logger._

/**
 *
 */
class ThreadService(val mailService: MailService) {
  // import implicit format BSON <--> Thread
  import wannaup.models.Threads._

  def replyTo(identificator: String): String = s"${identificator}-reply@inbound.domain.com"

  /**
   * Manage an incoming email, retrieve id and then search thread linked to email
   * @param email the incoming email
   * @return
   */
  def manage(email: Inbound): Future[Message] = {
    val id = BSONObjectID.parse(email.receivedEmail.replace("-reply@inbound.domain.com", ""))
    val message = Message(from = email.from.email, to = Some(email.to.head.email), body = email.html)
    id.map { oid =>
      Threads.c.find(BSONDocument("_id" -> oid)).one[Thread].flatMap {
        case Some(thread) =>
          val updatedThread = thread.copy(messages = thread.messages :+ message)
          val to = message.to.getOrElse(getFirstUtilTo(thread, message))
          val emailToSend = Email(
            subject = "",
            html = message.body,
            text = message.body,
            from = message.from,
            to = to,
            replyTo = replyTo(thread.id))
          mailService.send(emailToSend)
          Threads.c.save(updatedThread).map { lastError => message }
        case None =>
          log.info(s"hey, a thread was not found!! id: $id")
          throw new Exception(s"Oh shit! message with ($id) was not processed!")
      }
    }.recover {
      case e: Exception =>
        log.info(s"hey, an error!! e: ${e.getMessage}")
        Future.successful(message)
    }.get
  }

  /**
   * create a new thread, upon creation postman sends a mail containing the message to the to email address
   * setting the sender as the from mail address and the reply-to field to the email address of the mail node
   * (inbound.yourdomain.com).
   * @param owner of this thread
   * @param message to sent
   */
  def create(owner: User, message: Message): Future[Thread] = {
    val thread = Thread(owner = owner, messages = List(message))
    Threads.c.save(thread).map { lastError =>
      val email = Email(
        subject = "",
        html = message.body,
        text = message.body,
        from = message.from,
        to = message.to.get,
        replyTo = replyTo(thread.id))
      mailService.send(email)
      thread
    }
  }

  /**
   * return detail of a thread identified with id
   * @param id of the thread
   */
  def get(id: String): Future[Option[Thread]] = {
    Threads.c.find(BSONDocument("_id" -> BSONObjectID(id))).one[Thread]
  }

  /**
   * return all threads owned from a user
   * @param userId the owner of threads
   * @param limit
   * @param skip
   */
  def get(userId: String, limit: Int = 100, skip: Int = 0): Future[List[Thread]] = {
    Threads.c.find(BSONDocument("owner.id" -> userId)).options(QueryOpts(skip)).cursor[Thread].toList(limit)
  }

  /**
   * reply with a new message
   * @param threadId where reply
   * @param msg to reply with
   */
  def reply(threadId: String, message: Message): Future[Option[Thread]] = {
    Threads.c.find(BSONDocument("_id" -> BSONObjectID(threadId))).one[Thread].flatMap {
      case Some(thread) =>
        val to = message.to.getOrElse(getFirstUtilTo(thread, message))
        val newThread = thread.copy(messages = thread.messages :+ message.copy(to = Some(to)))
        Threads.c.update(BSONDocument("_id" -> BSONObjectID(threadId)), newThread, upsert = false, multi = false).map { lastError =>
          val email = Email(
            subject = "",
            html = message.body,
            text = message.body,
            from = message.from,
            to = to,
            replyTo = replyTo(thread.id))
          mailService.send(email)
          Some(newThread)
        }
      case None => Future.successful(None)
    }
  }

  /**
   * method for retrieve first util `to`
   * @param thread
   * @param message
   * @param string containing to
   */
  private def getFirstUtilTo(thread: Thread, message: Message): String = {
    thread.messages.find(m => m.from != message.from || m.to != Some(message.from)) match {
      case Some(m) if m.from != message.from     => m.from
      case Some(m) if m.to != Some(message.from) => m.to.get
      case None                                  => thread.messages.head.to.get // throw exception
    }
  }

}