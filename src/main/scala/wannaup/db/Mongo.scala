package wannaup.db

import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api._
import reactivemongo.api.collections._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import scala.util._
import wannaup.Config

/**
 *
 */
object Database extends Config {
  self: Config =>
  val driver = new Mongo(MongoSettings(config.getString("mongodb.uri")))
  val db = driver.db
}

object Mongo {
  // generate a valid id
  def generate = BSONObjectID.generate.stringify

  //  val command = reactivemongo.core.commands.Count("postman.thread", Some(reactivemongo.bson.BSONDocument()))
  //  val result = wannaup.db.Database.db.command(command)
}

class Mongo(config: MongoSettings) {

  // gets an instance of the driver (creates an actor system)
  val driver = new MongoDriver

  // get uri from configuration
  val uri: String = config.uri

  // 
  private val parsedUri = MongoConnection.parseURI(uri)

  // create connection with parsedUri
  val connection: MongoConnection = parsedUri.map { parsedUri =>
    driver.connection(parsedUri)
  }.getOrElse(driver.connection(List("localhost")))

  // get reference to db
  val db = connection.db(parsedUri.get.db.get)

  // method to use for start mongo object on boot
  def init = connection
}

case class MongoSettings(uri: String)