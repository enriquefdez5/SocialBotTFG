package model

// java
import java.util.Date

// twitter4j
import twitter4j.Status

case class Post(text: String, createdAt: Date, retweetedStatus: Status, getInReplyToUserId: Long,
                retweetedStatusUserId: Long)

