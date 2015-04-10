package wannaup.authenticators

import scala.concurrent.{ Future, ExecutionContext }
import spray.http.BasicHttpCredentials
import spray.routing.authentication._
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection._
import spray.routing.RequestContext
import wannaup.models._

/**
 * Spray types used by Authenticators (defined in spray.routing.authentication):
 *
 * type Authentication[T] = Either[Rejection, T]
 * type ContextAuthenticator[T] = RequestContext ⇒ Future[Authentication[T]]
 */
trait BasicAuthentication extends ContextAuthenticator[User] {
  type Identity = User
  // where execute concurrent work
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  
  /**
   *  this function retrieve something from request context
   *  @param ctx the context where retrieve information
   *  @return some parameter or none
   */
  def retrieve(ctx: RequestContext)(implicit ec: ExecutionContext): Future[Option[String]] = {
    Future.successful(ctx.request.headers.find {
      httpHeader => httpHeader.name == "Authorization"
    }.map(_.value.replace("Basic ", "")))
  }

  /**
   * Given a identifier retrieve an Identity
   * @param identifier a string (token) or other thing that identify a user
   * @return some identity or none
   */
  def retrieve(identifier: String)(implicit ec: ExecutionContext): Future[Option[Identity]] = {
    val credentials = BasicHttpCredentials(identifier)
    // we doesn't check password, because at moment we have no password set
    // if user is not found, we return a new user
    Users.find(credentials.username).map {
      case user: Some[User] => user
      case None             => Some(User(id = credentials.username, email = None))
    }
  }

  /**
   * @param ctx the context where retrieve information for authentication
   * @return an authentication
   */
  def apply(ctx: RequestContext): Future[Authentication[Identity]] = {
    retrieve(ctx).flatMap {
      case Some(identifier) =>
        retrieve(identifier).map {
          case Some(user) => Right(user)
          case None       => Left(AuthenticationFailedRejection(CredentialsRejected, List()))
        }
      case None => Future { Left(AuthenticationFailedRejection(CredentialsMissing, List())) }
    }
  }
}

object BasicAuthentication extends BasicAuthentication