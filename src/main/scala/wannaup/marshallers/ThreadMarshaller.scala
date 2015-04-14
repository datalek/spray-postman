package wannaup.marshallers

import spray.http._
import spray.http.MediaTypes._
import spray.httpx.unmarshalling._
import spray.httpx.PlayJsonSupport
import play.api.libs.json._
import wannaup.models._

/**
 *
 */
object ThreadMarshaller extends PlayJsonSupport {
  implicit val threadRestFormat = wannaup.formats.ThreadFormats.rest
  implicit val messageRestFormat = wannaup.formats.MessageFormats.rest
  implicit val messageWithMetaRestFormat = wannaup.formats.MessageFormats.restWithMeta
  implicit val inboundUnmarshaller = Unmarshaller[List[Inbound]](`multipart/form-data`, `application/x-www-form-urlencoded`, `text/plain`) {
    case HttpEntity.NonEmpty(contentType, data) =>
      implicit val a = wannaup.formats.InboundFormats.rest
      Json.parse(data.asString).validate[List[Inbound]] match {
        case JsSuccess(l, _) => l
        case JsError(e)      => throw new Exception(s"Received JSON is not valid.\n${Json.prettyPrint(JsError.toFlatJson(e))}")
      }
    // if we had meaningful semantics for the HttpEntity.Empty
    // we could add a case for the HttpEntity.Empty:
    // case HttpEntity.Empty => ...
  }
}