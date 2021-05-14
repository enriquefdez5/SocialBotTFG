package model

import java.util.Date

import twitter4j._

/** Case class that contains tweets information. It extends Status object from Twitter4j.
 *
 * @constructor Create a new StatusImpl with text, created at date, id of the user whose tweet has been retweeted,
 *              retweeted tweet and id of the replied tweet.
 * @param text Text content of the tweet.
 * @param createdAt Date when it was created.
 * @param rtUserId User identifier who was retweeted.
 * @param replyStatusId Tweet replied identifier.
 * @param rt Tweet retweeted.
 */
case class StatusImpl(text: String, createdAt: Date, rtUserId: Long, replyStatusId: Long, rt: Status)
  extends Status {
    val createdAtDate: Date = createdAt
    val textContent: String = text
    val rtStatus: Status = rt
    val currentUserRtId: Long = rtUserId
    val currentReplyId: Long = replyStatusId

    val notImplementedError = "Not implemented yet"

    /**
     * @return Tweet created at date.
     */
    override def getCreatedAt: Date = createdAtDate

    /**
     * @return Tweet text content.
     */
    override def getText: String = textContent

    /**
     * @return replied tweet identifier.
     */
    override def getInReplyToStatusId: Long = currentReplyId

    /**
     * @return replied user identifier.
     */
    override def getInReplyToUserId: Long = currentReplyId

    /**
     * @return true if this tweet is a retweet.
     */
    override def isRetweet: Boolean = rtStatus != null

    /**
     * @return retweeted tweet.
     */
    override def getRetweetedStatus: Status = rtStatus

    /**
     * @return current user retweet id.
     */
    override def getCurrentUserRetweetId: Long = currentUserRtId

    override def getId: Long =
        throw new UnsupportedOperationException(notImplementedError)

    override def getDisplayTextRangeStart: Int =
        throw new UnsupportedOperationException(notImplementedError)

    override def getDisplayTextRangeEnd: Int =
        throw new UnsupportedOperationException(notImplementedError)

    override def getSource: String =
        throw new UnsupportedOperationException(notImplementedError)

    override def isTruncated: Boolean =
        throw new UnsupportedOperationException(notImplementedError)

    override def getInReplyToScreenName: String =
        throw new UnsupportedOperationException(notImplementedError)

    override def getGeoLocation: GeoLocation =
        throw new UnsupportedOperationException(notImplementedError)

    override def getPlace: Place =
        throw new UnsupportedOperationException(notImplementedError)

    override def isFavorited: Boolean =
        throw new UnsupportedOperationException(notImplementedError)

    override def isRetweeted: Boolean =
        throw new UnsupportedOperationException(notImplementedError)

    override def getFavoriteCount: Int =
        throw new UnsupportedOperationException(notImplementedError)

    override def getUser: twitter4j.User =
        throw new UnsupportedOperationException(notImplementedError)

    override def getContributors: Array[Long] =
        throw new UnsupportedOperationException(notImplementedError)

    override def getRetweetCount: Int =
        throw new UnsupportedOperationException(notImplementedError)

    override def isRetweetedByMe: Boolean =
        throw new UnsupportedOperationException(notImplementedError)

    override def isPossiblySensitive: Boolean =
        throw new UnsupportedOperationException(notImplementedError)

    override def getLang: String =
        throw new UnsupportedOperationException(notImplementedError)

    override def getScopes: Scopes =
        throw new UnsupportedOperationException(notImplementedError)

    override def getWithheldInCountries: Array[String] =
        throw new UnsupportedOperationException(notImplementedError)

    override def getQuotedStatusId: Long =
        throw new UnsupportedOperationException(notImplementedError)

    override def getQuotedStatus: Status =
        throw new UnsupportedOperationException(notImplementedError)

    override def getQuotedStatusPermalink: URLEntity =
        throw new UnsupportedOperationException(notImplementedError)

    override def getUserMentionEntities: Array[UserMentionEntity] =
        throw new UnsupportedOperationException(notImplementedError)

    override def getURLEntities: Array[URLEntity] =
        throw new UnsupportedOperationException(notImplementedError)

    override def getHashtagEntities: Array[HashtagEntity] =
        throw new UnsupportedOperationException(notImplementedError)

    override def getMediaEntities: Array[MediaEntity] =
        throw new UnsupportedOperationException(notImplementedError)

    override def getSymbolEntities: Array[SymbolEntity] =
        throw new UnsupportedOperationException(notImplementedError)

    override def getRateLimitStatus: RateLimitStatus =
        throw new UnsupportedOperationException(notImplementedError)

    override def getAccessLevel: Int =
        throw new UnsupportedOperationException(notImplementedError)

    override def compareTo(o: Status): Int =
        throw new UnsupportedOperationException(notImplementedError)

}
