package wannaup

import akka.io.IO
import akka.util.Timeout
import akka.pattern.ask
import akka.actor.{ ActorSystem, Props }
import spray.can.Http
import scala.concurrent.duration._
import com.typesafe.config._

import wannaup.util.Logger._
import wannaup.routes._
import wannaup.services._

/**
 *
 */
object Boot extends App with Config {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  //connect to DB
  log.debug(s"started db: ${wannaup.db.Database.driver.uri}")

  // create service to inject into our route
  val mandrillService = new MandrillService(MandrillSettings(key = config.getString("mandrill.key")))
  val threadService = new ThreadService(mandrillService)
  
  // create and start our service actor
  val service = system.actorOf(Props(new ThreadRouteActor(threadService)), "postman-service")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = config.getString("server.host"), port = config.getInt("server.port"))

  sys.addShutdownHook(system.shutdown())

}

/**
 *
 */
trait Config {
  val config = ConfigFactory.load()
}