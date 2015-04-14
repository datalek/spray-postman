package wannaup.formats

import play.api.libs.json._
import play.api.libs.functional.syntax._
import wannaup.models._
import reactivemongo.bson.BSONObjectID

import reactivemongo.bson._

/**
 *
 */
object MessageBSONFormat extends BSONDocumentReader[Message] with BSONDocumentWriter[Message] {
  def read(doc: BSONDocument): Message = {
    Message(
      from = doc.getAs[String]("from").get,
      to = doc.getAs[String]("to"),
      body = doc.getAs[String]("body").get)
  }
  def write(msg: Message): BSONDocument = {
    BSONDocument(
      "from" -> msg.from,
      "to" -> msg.to,
      "body" -> msg.body)
  }
}

object ThreadBSONFormat extends BSONDocumentReader[Thread] with BSONDocumentWriter[Thread] {
  implicit val userFormat = wannaup.formats.UserBSONFormat
  implicit val messageFormat = wannaup.formats.MessageBSONFormat
  def read(doc: BSONDocument): Thread = {
    Thread(
      id = doc.getAs[BSONObjectID]("_id").get.stringify,
      owner = doc.getAs[User]("owner").get,
      meta = doc.getAs[String]("meta"),
      messages = doc.getAs[List[Message]]("msgs").toList.flatten)
  }
  def write(thread: Thread): BSONDocument = {
    BSONDocument(
      "_id" -> BSONObjectID(thread.id),
      "owner" -> thread.owner,
      "meta" -> thread.meta,
      "msgs" -> thread.messages)
  }
}

/**
 *
 */
object ThreadFormats {
  val rest = {
    implicit val userFormat = UserFormats.rest
    implicit val messageFormat = MessageFormats.rest
    val reader = (
      (__ \ "id").readNullable[String].map(_.getOrElse(BSONObjectID.generate.stringify)) ~
      (__ \ "owner").read[User] ~
      (__ \ "meta").readNullable[JsValue].map(_.map(Json.stringify(_))) ~
      ((__ \ "msgs").read(Reads.list[Message]) orElse
        // TODO: check if the to key exists
        (__ \ "msg").read[Message](MessageFormats.strictRead).map { msg => List(msg) }))(Thread.apply _)
    val writer = (
      (__ \ "id").write[String] ~
      (__ \ "owner").write[User] ~
      (__ \ "meta").writeNullable[JsValue].contramap[Option[String]](_.map(Json.parse(_))) ~
      (__ \ "msgs").write(Writes.list[Message]))(unlift(Thread.unapply _))

    Format(reader, writer)
  }
}

/**
 *
 */
object MessageFormats {
  val rest: Format[Message] = {
    val reader = (
      (__ \ "from").read[String](Reads.email) ~
      (__ \ "to").readNullable[String](Reads.email) ~
      (__ \ "body").read[String])(Message.apply _)
    val writer = Json.writes[Message]
    Format(reader, writer)
  }
  
  val restWithMeta: Format[Message with Meta] = {
    val reader = (
      (__ \ "from").read[String](Reads.email) ~
      (__ \ "to").readNullable[String](Reads.email) ~
      (__ \ "body").read[String] ~
      (__ \ "meta").readNullable[JsValue].map(_.map(Json.stringify(_))))(MessageWithMeta.apply _)
    val writer = (
      (__ \ "from").write[String] ~
      (__ \ "to").writeNullable[String] ~
      (__ \ "body").write[String] ~
      (__ \ "meta").writeNullable[JsValue].contramap[Option[String]](_.map(Json.parse(_))) )(MessageWithMeta.unapply _)
    Format(reader, writer)
  }

  /**
   * Require `to` key into json
   */
  val strictRead = (
    (__ \ "from").read[String](Reads.email) ~
    (__ \ "to").read[String](Reads.email) ~
    (__ \ "body").read[String])((from, to, body) => Message(from = from, to = Some(to), body = body))
}