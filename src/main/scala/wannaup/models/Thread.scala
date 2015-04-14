package wannaup.models

import wannaup.db._

/**
 * This class represents a single message
 *
 * @param from
 * @param to
 * @param body
 */
case class Message(
  from: String,
  to: Option[String],
  body: String)

object MessageWithMeta {
  def apply(from: String, to: Option[String], body: String, meta: Option[String]) = {
    // use this, otherwise meta = meta is recursive and end in a infinite loop
    val _meta = meta
    new Message(from, to, body) with Meta {
      def meta: Option[String] = _meta
    }
  }
  def unapply(m: Message with Meta) = {
    (m.from, m.to, m.body, m.meta)
  }
}

trait Meta {
  def meta: Option[String]
}

/**
 * This class represent a thread
 *
 * @param id
 * @param owner
 * @param messages
 */
case class Thread(
  id: String = Threads.generate,
  owner: User,
  meta: Option[String] = None,
  messages: List[Message] = List())

object Threads {
  def generate = Mongo.generate
  implicit val threadFormat = wannaup.formats.ThreadBSONFormat
  val c = wannaup.db.Database.db.collection("postman.thread")
}