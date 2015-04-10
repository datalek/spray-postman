package wannaup.routes

import scala.concurrent._
import akka.actor._
import spray.http.StatusCodes
import spray.routing.HttpService
import spray.routing.Directives._
import wannaup.marshallers.ThreadMarshaller
import wannaup.services.ThreadService
import wannaup.authenticators._
import wannaup.models._

//
object ThreadRouteActor {
  // Factory method for our actor Props
  def props(threadService: ThreadService): Props = Props(new ThreadRouteActor(threadService))
  
//  def props(threadService: ActorRef): Props = Props(new ThreadRouteActor(threadService))
}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ThreadRouteActor(val threadService: ThreadService) extends Actor with ThreadRoute {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(route)

}

trait ThreadRoute extends HttpService {

  import ThreadMarshaller._
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  
  def threadService: ThreadService

  val route = {
    (path("inbound") & pathEndOrSingleSlash) {
      post {
        formFields('mandrill_events.as[List[Inbound]]) { events =>
          val resp = events.map { email =>
            threadService.manage(email)
          }
          complete(Future.sequence(resp))
        }
      }
    } ~
    (path("threads") & pathEndOrSingleSlash) {
      post {
        entity(as[Message]) { message =>
          authenticate(BasicAuthentication) { user =>
            val resp = threadService.create(owner = user, message = message)
            complete(resp)
          }
        }
      } ~
      get {
        authenticate(BasicAuthentication) { user =>
          val resp = threadService.get(user.id, 100, 0)
          complete(resp)
        }
      }
    } ~
    path("threads" / Segment) { threadId =>
      get {
        authenticate(BasicAuthentication) { user =>
          val resp = threadService.get(threadId)
          complete(resp)
        }
      }
    } ~
    path("threads" / Segment / "reply") { threadId =>
      post {
        entity(as[Message]) { message =>
          authenticate(BasicAuthentication) { user =>
            val resp = threadService.reply(threadId, message)
            complete(resp)
          }
        }
      }
    }
  }
}
