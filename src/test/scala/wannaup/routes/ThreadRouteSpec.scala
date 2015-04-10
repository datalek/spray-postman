package wannaup.routes

import scala.concurrent._
import scala.concurrent.duration._
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import play.api.libs.json._
import reactivemongo.bson._
import wannaup.services._
import wannaup.models._
import wannaup.marshallers.ThreadMarshaller._
import testdata._

/**
 *
 */
class ThreadRouteSpec extends Specification with Specs2RouteTest
  with NoTimeConversions
  with GeneralSettings {
  import Threads._
  // we need to run tests sequentially, because on one of it we drop database
  sequential
  // Create service to test with mock or stub injected
  val mandrillService = new MandrillService(MandrillSettings(key = "mandrill.key"))
  val threadRoute = new ThreadRoute {
    val actorRefFactory = system
    val threadService = new ThreadService(mandrillService)
  }

  // tests for Thread service
  "ThreadRoute" should {

    "receiving POST request to /inbound" in {
      val tId = ThreadData.thread0.id
      Await.result(Threads.c.save(ThreadData.thread0), 5.seconds)
      Post("/inbound", FormData(Map("mandrill_events" -> InboundTestData.body(to = tId)))) ~> threadRoute.route ~> check {
        val dbThread = Await.result(Threads.c.find(BSONDocument("_id" -> BSONObjectID(tId))).one[Thread], 5.seconds)
        dbThread.get.messages.length should be equalTo (2)
        response.status should be(StatusCodes.OK)
      }
    }

    "create a new thread when POST a message to /threads" in {
      val authHeader = HttpHeaders.`Authorization`(BasicHttpCredentials(UserData.user1.id, "doesn't matter man"))
      Post("/threads", MessageData.msg0) ~> addHeader(authHeader) ~> threadRoute.route ~> check {
        response.status should be(StatusCodes.OK)
        val respThread = responseAs[Thread]
        val dbThread = Await.result(Threads.c.find(BSONDocument("_id" -> BSONObjectID(respThread.id))).one[Thread], 5.seconds)
        responseAs[Thread] must be equalTo (dbThread.get)
      }
    }
    //TODO: finish it we need error, BadRequest
    //    "create a new thread when POST a message to /threads without `to` key" in {
    //      val authHeader = HttpHeaders.`Authorization`(BasicHttpCredentials(UserData.user1.id, "doesn't matter man"))
    //      val message = MessageData.msg0.copy(to = None)
    //      Post("/threads", message) ~> addHeader(authHeader) ~> threadRoute.route ~> check {
    //        response.status should be(StatusCodes.OK)
    //        val respThread = responseAs[Thread]
    //        val dbThread = Await.result(Threads.c.find(BSONDocument("_id" -> BSONObjectID(respThread.id))).one[Thread], 5.seconds)
    //        responseAs[Thread] must be equalTo (dbThread.get)
    //      }
    //    }

    "reply in a thread when POST a new message to /threads/:theardId/reply" in {
      val authHeader = HttpHeaders.`Authorization`(BasicHttpCredentials(ThreadData.thread0.owner.id, "doesn't matter man"))
      Await.result(Threads.c.save(ThreadData.thread0), 5.seconds)
      val threadId = ThreadData.thread0.id
      val message = MessageData.msg3
      Post(s"/threads/$threadId/reply", message) ~> addHeader(authHeader) ~> threadRoute.route ~> check {
        response.status should be(StatusCodes.OK)
        responseAs[Thread].messages must not be equalTo(ThreadData.thread0.messages)
        responseAs[Thread].messages.last must be equalTo message
      }
    }

    "reply in a thread when POST a new message to /threads/:theardId/reply with `to` void" in {
      val authHeader = HttpHeaders.`Authorization`(BasicHttpCredentials(ThreadData.thread0.owner.id, "doesn't matter man"))
      // this thread data contains 3 messages, the last two with `from` and `to` equals, the first with to different from other 
      // (A to B), (B to A), (B to A) ===> B -> A.?
      val threadDb = ThreadData.thread1
      Await.result(Threads.c.save(threadDb), 5.seconds)
      val threadId = ThreadData.thread0.id
      val message = MessageData.msg0.copy(to = None)
      Post(s"/threads/$threadId/reply", message) ~> addHeader(authHeader) ~> threadRoute.route ~> check {
        response.status should be(StatusCodes.OK)
        responseAs[Thread].messages.last.to must be equalTo Some(threadDb.messages.head.from)
      }
    }

    "reply in a thread that doesn't exists when POST a new message to /threads/:theardId/reply" in {
      val authHeader = HttpHeaders.`Authorization`(BasicHttpCredentials(ThreadData.thread0.owner.id, "doesn't matter man"))
      val threadId = ThreadData.thread2.id
      Post(s"/threads/$threadId/reply", MessageData.msg0) ~> addHeader(authHeader) ~> threadRoute.route ~> check {
        response.status should be(StatusCodes.NotFound)
      }
    }

    "return a detail of a thread when GET thread detail to /threads/:threadId" in {
      val authHeader = HttpHeaders.`Authorization`(BasicHttpCredentials(ThreadData.thread0.owner.id, "doesn't matter man"))
      Await.result(Threads.c.save(ThreadData.thread0), 5.seconds)
      val threadsId = ThreadData.thread0.id
      Get(s"/threads/$threadsId") ~> addHeader(authHeader) ~> threadRoute.route ~> check {
        response.status should be(StatusCodes.OK)
        responseAs[Thread] must be equalTo (ThreadData.thread0)
      }
    }

    "return void threads of a user that doesn't owns thread when GET threads to /threads" in {
      val authHeader = HttpHeaders.`Authorization`(BasicHttpCredentials(UserData.user3.id, "doesn't matter man"))
      Get("/threads") ~> addHeader(authHeader) ~> threadRoute.route ~> check {
        response.status should be(StatusCodes.OK)
        responseAs[List[Thread]].length must be equalTo 0
        responseAs[List[Thread]].toSet must equalTo(Set())
      }
    }

    "return all threads of a user when GET threads to /threads" in DropDatabaseBefore {
      val threadOfUser = ThreadData.threads.filter(_.owner.id == ThreadData.thread0.owner.id)
      val authHeader = HttpHeaders.`Authorization`(BasicHttpCredentials(ThreadData.thread0.owner.id, "doesn't matter man"))
      val futureThreads = ThreadData.threads.map { Threads.c.save(_) }
      Await.result(Future.sequence(futureThreads), 5.seconds)
      Get("/threads") ~> addHeader(authHeader) ~> threadRoute.route ~> check {
        response.status should be(StatusCodes.OK)
        responseAs[List[Thread]].length must be equalTo (ThreadData.threads.length)
        responseAs[List[Thread]].toSet must equalTo(threadOfUser.toSet)
      }
    }

    "return 401 if GET threads to /threads with bad credentials" in {
      Get("/threads") ~> threadRoute.sealRoute(threadRoute.route) ~> check {
        response.status should be(StatusCodes.Unauthorized)
      }
    }
  }

}