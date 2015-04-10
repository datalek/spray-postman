package wannaup.routes

import spray.http.StatusCodes
import spray.httpx._
import spray.routing._
import spray.util.LoggingContext

/**
 * Handle exception, this trait will be composed with services that want handle
 *  exception in different ways respect default
 */
trait RouteExceptionHandlers extends HttpService {

  implicit def exceptionHandler(implicit log: LoggingContext) = ExceptionHandler {
    case e: UnsuccessfulResponseException =>
      requestUri { uri =>
        log.warning("Request to {} could not be handled normally", uri)
        complete(e.response.status)
      }
    case t: Throwable =>
      requestUri { uri =>
        log.warning("Request to {} could not be handled normally", uri)
        complete(StatusCodes.InternalServerError, "Wouldn't you like to know what happened?")
      }
  }

}