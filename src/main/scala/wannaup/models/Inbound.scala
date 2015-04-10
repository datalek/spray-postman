package wannaup.models

/**
 * 
 */
case class UserEmail(name: String, email: String)

case class Inbound(
    id: String,
    timeStamp: Int,
    html: String,
    text: String,
    subject: String,
    from: UserEmail,
    to: List[UserEmail],
    receivedEmail: String,
    headers: Map[String, String]
)
