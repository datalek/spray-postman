package wannaup.models

import scala.concurrent.{ Future, ExecutionContext }
import wannaup.db._

/**
 * Represents a user
 *
 * @param id
 * @param email
 */
case class User(
  id: String,
  email: Option[String])

object Users {
  
  import reactivemongo.bson._
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val threadFormat = wannaup.formats.ThreadBSONFormat
  
  val c = wannaup.db.Database.db.collection("postman.thread")
  
  def find(id: String): Future[Option[User]] = {
    c.find(BSONDocument("owner.id" -> id)).one[Thread].map {
      case Some(thread) => Some(thread.owner)
      case None         => None
    }
  }
}