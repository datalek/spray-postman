package wannaup.formats

import reactivemongo.bson._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import wannaup.models._

/**
 *
 * The webhook request is a standard POST request with a single parameter - mandrill_events.
 * This parameter will contain a JSON-encoded array of the messages that match the webhook information.
 * Each element in this array is a JSON object with the following.
 * @link http://help.mandrill.com/entries/22092308-What-is-the-format-of-inbound-email-webhooks-
 * es:
 *
 *  {
 *    ts: 123123, //timestamp
 *    event: "inbound",
 *    msg: {
 *      html: "",
 *      text: "",
 *      subject: "",
 *      from_email: "",
 *      from_name: "",
 *      to: [{
 *        email: "",
 *        name: ""
 *      }],
 *      email: "",
 *      headers:{
 *        key0: "",
 *        key1: "",
 *        ...
 *      }
 *    }
 *  }
 *
 */
object InboundFormats {
  val rest = {
    implicit val userFormat = Json.format[UserEmail]
    val reader = (
      (__ \ "id").readNullable[String].map(_.getOrElse(BSONObjectID.generate.stringify)) ~
        (__ \ "ts").read[Int] ~
        (__ \ "msg" \ "html").read[String] ~
        (__ \ "msg" \ "text").read[String] ~
        (__ \ "msg" \ "subject").read[String] ~
        ((__ \ "msg" \ "from_email").read[String] ~
        (__ \ "msg" \ "from_name").read[String])(UserEmail.apply _) ~
        (__ \ "msg" \ "to").read(Reads.list[UserEmail]) ~
        (__ \ "msg" \ "email").readNullable[String].map(_.getOrElse("")) ~
        (__ \ "msg" \ "headers").readNullable[Map[String, String]].map(_.getOrElse(Map())))(Inbound.apply _)
    reader
  }

}