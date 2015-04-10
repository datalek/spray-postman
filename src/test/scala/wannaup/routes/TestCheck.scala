//package wannaup.routes
//
//import org.specs2.mutable._
//import org.specs2.specification._
//
//import spray.testkit.Specs2RouteTest
//
//trait DatabaseSetup extends SpecificationLike {
//  protected def beforeAll() = {
//    println("beforeAll")
//  }
//
//  protected def afterAll() = {
//    //cleanup-code to be executed after each isolated test
//    println("afterAll")
//  }
//
//  override def map(fs: => Fragments) = Step(beforeAll) ^ fragments ^ Step(afterAll)
//}
//
//trait Test extends org.specs2.specification.BeforeAfterExample {
//  def before = println("before")
//  def after = println("after")
//}
//
//class TestCheck extends Specification with Specs2RouteTest with DatabaseSetup {
//
//  sequential
//
//  "First" should {
//    println("First")
//    "fare la popo" in {
//      println("First - fare la popo")
//      1 must be equalTo 1
//    }
//    "fare la pupu" in {
//      println("First - fare la pupu")
//      1 must be equalTo 1
//    }
//  }
//
//  "Seconds" should {
//    println("Seconds")
//    "fare la popo" in {
//      println("Seconds - fare la popo")
//      1 must be equalTo 1
//    }
//    "fare la pupu" in {
//      println("Seconds - fare la pupu")
//      1 must be equalTo 1
//    }
//  }
//}