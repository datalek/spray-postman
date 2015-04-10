package wannaup.services

import scala.concurrent.Future
import scala.util._
import akka.actor._
import spray.http._
import spray.client.pipelining._
import spray.httpx.PlayJsonSupport
import play.api.libs.json._
import play.api.libs.functional.syntax._
import wannaup.Config

/**
 *
 */
case class Response(status: String, code: Int, message: String)

/**
 *
 */
case class Email(
  html: String,
  text: String,
  subject: String,
  from: String,
  to: String,
  replyTo: String)

/**
 * Common mail service
 */
trait MailService {

  def send(email: Email): Future[Try[Email]]

}

object MandrillService {
  // Marshaller for conversion Json <--> Email
  object MandrillMarshaller extends PlayJsonSupport {
    implicit val responseFormat: Format[Response] = Json.format[Response]
    implicit val mailFormat: Format[Email] = (
      (__ \ "html").format[String] ~
      (__ \ "text").format[String] ~
      (__ \ "subject").format[String] ~
      (__ \ "from").format[String] ~
      (__ \ "to").format[String] ~
      (__ \ "replyTo").format[String])((Email.apply), unlift(Email.unapply))
  }
}

/**
 *
 */
class MandrillService(config: MandrillSettings)(implicit system: ActorSystem) extends MailService {
  
  import MandrillService._
  import MandrillService.MandrillMarshaller._

  import system.dispatcher // execution context for futures

  val key: String = config.key

  /**
   * @param email to send
   * @return email if operation success else none
   */
  def send(email: Email): Future[Try[Email]] = {
    val pipeline = sendReceive ~> unmarshal[Response]
    val response: Future[Response] = pipeline(Post("http://example.com/orders", email))
    response.map {
      case Response("ok", 200, message)    => Success(email)
      case Response(status, code, message) => Failure(new Exception(s"$status - $code - $message"))
    }
  }
}

/**
 *
 */
case class MandrillSettings(key: String)
