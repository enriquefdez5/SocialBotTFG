package model

// java
import java.util.Date

// twitter4j
import twitter4j.Status

/**
 * Case class that represents a twitter4j Status object transformed into this app model. It only contains what is
 * interesting for
 * @param text, String. String that represents the text contained in the Status object.
 * @param createdAt, Date. Date that represents the date when the Status object was created.
 * @param retweetedStatus, Status. Status object that is the retweeted Status if if exists.
 *        It could be null if it is not a retweet.
 * @param retweetedStatusUserId, Long. Long that represents the id of the retweeted user.
 *        It could be 0 if it is not a retweet.
 * @param getInReplyToUserId, Long. Long that represents the id of the replied user.
 *        It could be 0 if it is not a reply.
 */
case class Post(text: String, createdAt: Date, retweetedStatus: Status, retweetedStatusUserId: Long,
                getInReplyToUserId: Long)

