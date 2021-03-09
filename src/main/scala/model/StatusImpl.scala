package model

import java.util.Date

import twitter4j._

class StatusImpl(text: String, createdAt: Date, status: StatusImpl, rtUserId: Long, replyStatusId: Long  ) extends
  Status {
  val createdAtDate: Date = createdAt
  val textContent: String = text
  val rtStatus: StatusImpl = status
  val currentUserRtId: Long = rtUserId
  val currentReplyId: Long = replyStatusId

  override def getCreatedAt: Date = createdAtDate

  override def getId: Long = ???

  override def getText: String = textContent

  override def getDisplayTextRangeStart: Int = ???

  override def getDisplayTextRangeEnd: Int = ???

  override def getSource: String = ???

  override def isTruncated: Boolean = ???

  override def getInReplyToStatusId: Long = currentReplyId

  override def getInReplyToUserId: Long = ???

  override def getInReplyToScreenName: String = ???

  override def getGeoLocation: GeoLocation = ???

  override def getPlace: Place = ???

  override def isFavorited: Boolean = ???

  override def isRetweeted: Boolean = ???

  override def getFavoriteCount: Int = ???

  override def getUser: twitter4j.User = ???

  override def isRetweet: Boolean = rtStatus != null

  override def getRetweetedStatus: Status = rtStatus

  override def getContributors: Array[Long] = ???

  override def getRetweetCount: Int = ???

  override def isRetweetedByMe: Boolean = ???

  override def getCurrentUserRetweetId: Long = currentUserRtId

  override def isPossiblySensitive: Boolean = ???

  override def getLang: String = ???

  override def getScopes: Scopes = ???

  override def getWithheldInCountries: Array[String] = ???

  override def getQuotedStatusId: Long = ???

  override def getQuotedStatus: Status = ???

  override def getQuotedStatusPermalink: URLEntity = ???

  override def getUserMentionEntities: Array[UserMentionEntity] = ???

  override def getURLEntities: Array[URLEntity] = ???

  override def getHashtagEntities: Array[HashtagEntity] = ???

  override def getMediaEntities: Array[MediaEntity] = ???

  override def getSymbolEntities: Array[SymbolEntity] = ???

  override def getRateLimitStatus: RateLimitStatus = ???

  override def getAccessLevel: Int = ???

  override def compareTo(o: Status): Int = ???
}
