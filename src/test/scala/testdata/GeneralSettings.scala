package testdata

import scala.concurrent._
import scala.concurrent.duration._
import org.specs2.mutable._
import org.specs2.specification._
import spray.testkit._

import wannaup.db.Database

/**
 * Clean everything before and after Test 
 */
trait GeneralSettings extends SpecificationLike {
  this: Specs2RouteTest =>

  implicit val routeTestTimeout = RouteTestTimeout(Duration(5, SECONDS))

  val db = wannaup.db.Database.db
  def beforeAll = {
    Await.result(db.drop(), Duration(10, SECONDS))
  }
  def afterAll = {
    Await.result(db.drop(), Duration(10, SECONDS))
    system.shutdown()
    //        db.connection.actorSystem.shutdown()
  }

  override def map(fs: => Fragments) = Step(beforeAll) ^ fragments ^ Step(afterAll)
}

/**
 * This helper class drop database before execution of Example block
 * remember note: This have to be an object, otherwise doesn't works
 * @example:
 *    "description oh yeah" in DropDatabaseBefore {
 *        // the database is clean before run above code
 *        //put your assert here
 *    }
 */
object DropDatabaseBefore extends BeforeEach {
  import scala.concurrent.ExecutionContext.Implicits.global
  def before = {
    Await.result(Database.db.drop(), 10.seconds)
  }
}