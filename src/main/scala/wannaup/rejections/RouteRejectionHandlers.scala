package wannaup.routes

import spray.http._
import spray.routing._
import spray.httpx.unmarshalling.MalformedContent
import spray.routing.Directives

/**
 * Handle rejection, this trait will be composed with services that want handle
 *  rejection in different ways respect default
 */
trait RouteRejectionHandlers extends HttpService {

  implicit val rejectionHandler = RejectionHandler {
    case MalformedRequestContentRejection(message, e) :: _ => requestUri { uri =>
      respondWithMediaType(MediaTypes.`application/json`) {
        println(e)
        import play.api.libs.json._
        val json = Json.obj("status" -> "ko", "message" -> message)
        complete(StatusCodes.BadRequest, json.toString)
      }
    }
  }

}
