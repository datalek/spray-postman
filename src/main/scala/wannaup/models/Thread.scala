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
  messages: List[Message] = List())

object Threads {
  def generate = Mongo.generate
  implicit val threadFormat = wannaup.formats.ThreadBSONFormat
  val c = wannaup.db.Database.db.collection("postman.thread")
}