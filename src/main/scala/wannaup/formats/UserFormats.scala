package wannaup.formats

import play.api.libs.json._
import play.api.libs.functional.syntax._
import wannaup.models.{ User }

import reactivemongo.bson._

/**
 * 
 */
object UserBSONFormat extends BSONDocumentReader[User] with BSONDocumentWriter[User] {
  def read(doc: BSONDocument): User = {
    User(
      id = doc.getAs[String]("id").get,
      email = doc.getAs[String]("email"))
  }
  def write(user: User): BSONDocument = {
    BSONDocument(
      "id" -> user.id,
      "email" -> user.email)
  }
}

/**
 *
 */
object UserFormats {
  val rest = {
    val reader = (
      (__ \ "id").read[String] ~
      (__ \ "email").readNullable[String](Reads.email))(User.apply _)
    val writer = (
      (__ \ "id").write[String] ~
      (__ \ "email").writeNullable[String])(unlift(User.unapply _))

    Format(reader, writer)

  }
}