package testdata

import wannaup.models._
import play.api.libs.json._

/**
 *
 */
object UserData {
  val user0 = User(id = "0", email = Some("user0@mail.com"))
  val user1 = User(id = "1", email = Some("user1@mail.com"))
  val user2 = User(id = "2", email = Some("user2@mail.com"))
  val user3 = User(id = "3", email = Some("user3@mail.com"))

  val users = List(user0, user1, user2, user3)
}

/**
 *
 */
object MessageData {
  val msg0 = Message(from = UserData.user0.email.get, to = Some(UserData.user1.email.get), body = "hello!")
  val msg1 = Message(from = UserData.user1.email.get, to = Some(UserData.user0.email.get), body = "Hi!")
  val msg2 = Message(from = UserData.user0.email.get, to = Some(UserData.user1.email.get), body = "Hey!")
  val msg3 = Message(from = UserData.user0.email.get, to = Some(UserData.user1.email.get), body = "I'm batman!")
  val msg4 = Message(from = UserData.user1.email.get, to = Some(UserData.user0.email.get), body = "WTF!")

  val messages = List(msg0, msg1, msg2, msg3, msg4)
}

/**
 *
 */
object ThreadData {
  implicit val userFormat = wannaup.formats.UserFormats.rest
  implicit val msgFormat = wannaup.formats.MessageFormats.rest

  val json =
    Json.obj(
      "to" -> UserData.user0.email,
      "from" -> UserData.user1.email,
      "msg" -> "hello!")

  val thread0 = Thread(owner = UserData.user0, messages = List(MessageData.msg0))
  val thread1 = Thread(owner = UserData.user0, messages = List(MessageData.msg1, MessageData.msg0, MessageData.msg0))
  val thread2 = Thread(owner = UserData.user0, messages = List(MessageData.msg0))

  val threads = List(thread0, thread1, thread2)
}

object InboundTestData {
  
  def body(to: String) = s"""
    [{
      "ts": 198743897,
      "event": "inbound",
      "msg": {
          "html": "<p>Hey!</p>",
          "text": "Hey!",
          "subject": "this is subject",
          "from_email": "gino.random@test.com",
          "from_name": "Gino Random",
          "email": "${to}-reply@inbound.domain.com",
          "to": [
            {
                  "email": "ciao@email.com",
                  "name": "Pallone Lavegas"
              }
          ]
      }
    }]
    """
}