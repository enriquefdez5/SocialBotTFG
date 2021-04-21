package twitterapiTest

import java.util
import java.util.Date

import model.exceptions.IncorrectSizeListException
import model.StatusImpl
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import twitter4j.Status
import app.twitterAPI.TwitterServiceOperations.{getLastTweetNotReplied, getLastTweetNotRetweeted, obtainMaxActionsPerHour, getMeanActionsPerHour, obtainMostRepliedUserId, obtainMostRetweetedUserId, getMaxFollowedPostActions, obtainRtsInfo, statusesToStatusImpl}

class TwitterServiceOperationsTest {

  val emptyListExceptionMessage: String = "List can not be empty"
  val csvSeparator: String = ","

  val rtId: String = "3"
  val notExistingId = -1
  val existingUserId: Int = 123456789
  val date: Date = new Date()
  val thisIsAPostText: String = "This is a post"
  val tweetPost: StatusImpl = StatusImpl(thisIsAPostText, date, notExistingId, notExistingId, null)
  val replyPost: StatusImpl = StatusImpl("This is a reply", date, notExistingId, existingUserId, null)
  val rtPost: StatusImpl = StatusImpl("This is a rt post", date, existingUserId, notExistingId, new StatusImpl
  ("anotherPost", date, notExistingId, notExistingId, null))

  val firstTextShard: String = "texto pos 0\ttexto pos 1\ttexto pos " +
    "2\t2021-01-02\t15:30:58\tusername\tusername\t26\t69\t2791" +
    "\t\"This is a normal tweet posted\"\t\t\t"
  val secondTextShard: String = "\t\t1303310580464398336" +
    "\thttps://twitter.com/username/status/1303307843391684609\t\t\t\t\t\t\t\t\t\t\t\t\t\t\treplyUser"
  val csvTweetPost: String = firstTextShard + secondTextShard
  val csvTweetPost2: String = firstTextShard + "uhuhuh" + secondTextShard
  val csvTweetPost3: String = firstTextShard + "xdxdxd" + secondTextShard


  val csvReplyPost: String = "texto pos 0\ttexto pos 1\ttexto pos " +
    "2\t2021-02-27\t12:33:51\tusername\tusername\t26\t69\t2791" +
    "\t\"buen cochazo se va notando el sueldo de caster\"\t\t\t\t\t1303310580464398336" +
    "\thttps://twitter.com/username/status/1303310580464398336\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
    "[{'screen_name': 'username', 'name': 'Rocket \uD83D\uDE80\uD83C\uDF38', 'id': '160908260'}]"


  @Test
  def statusesToPostTest(): Unit = {
    // empty seq
    val emptyStatusSeq: Seq[Status] = Seq[Status]()
    try {
      statusesToStatusImpl(emptyStatusSeq)
      assertEquals(emptyStatusSeq.length, statusesToStatusImpl(emptyStatusSeq).length)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }


    // 2 status to posts
    val status1: StatusImpl = StatusImpl("Status 1 text", date, existingUserId, notExistingId, null)
    val status2: StatusImpl = StatusImpl("Status 1 text", date, existingUserId, notExistingId, null)
    val statusSeq: Seq[Status] = Seq[Status](status1, status2)
    val postSeq: Seq[StatusImpl] = statusesToStatusImpl(statusSeq)
    assertEquals(statusSeq.length, postSeq.length)
    assertEquals(statusSeq.head.getText, postSeq.head.text)
    assertEquals(statusSeq.head.getCreatedAt, postSeq.head.createdAt)
  }

  @Test
  def getLastTweetNotRetweetedTest(): Unit = {
    // no tweets on seq
    val postSeq: Seq[Status] = Seq[Status]()
    try {
      getLastTweetNotRetweeted(postSeq, 0)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // rt on seq
    val status1: Status = new StatusImpl("This is a rt", date, existingUserId, notExistingId,
      StatusImpl("anotherPost", date, notExistingId, notExistingId, null))
    val postSeq2: Seq[Status] = Seq[Status](status1)
    assertEquals(null, getLastTweetNotRetweeted(postSeq2, 0))


    // no rt on seq
    val status2: Status = StatusImpl(thisIsAPostText, date, notExistingId, notExistingId, null)
    val status3: Status = StatusImpl(thisIsAPostText, date, notExistingId, notExistingId, null)
    val postSeq3: Seq[Status] = Seq[Status](status2, status3)
    val lastTweetNotRetweeted = getLastTweetNotRetweeted(postSeq3, 0)
    assertEquals(status2.getText, lastTweetNotRetweeted.getText)
    assertEquals(status2.getCreatedAt, lastTweetNotRetweeted.getCreatedAt)
  }

  @Test
  def getLastTweetNotRepliedTest(): Unit = {
    // no tweets on seq
    val postSeq1: Seq[Status] = Seq[Status]()
    try {
      getLastTweetNotReplied(postSeq1, 0)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // a reply on seq
    val status1: StatusImpl = StatusImpl("This is a reply", date, notExistingId, existingUserId, null)
    val postSeq2: Seq[Status] = Seq[Status](status1)
    // no replies on seq
    val status2: StatusImpl = StatusImpl("This is not a reply", date, notExistingId, notExistingId, null)
    val status3: StatusImpl = StatusImpl("This is not a reply", date, notExistingId, notExistingId, null)
    val postSeq3: Seq[Status] = Seq[Status](status2, status3)
    val lastTweetNotReplied = getLastTweetNotReplied(postSeq3, 0)
    assertEquals(status2.getText, lastTweetNotReplied.getText)
    assertEquals(status2.getCreatedAt, lastTweetNotReplied.getCreatedAt)
  }

  @Test
  def obtainMostRetweetedUserIdTest(): Unit = {
    // There are no posts
    val emptyPostSeq: Seq[StatusImpl] = Seq[StatusImpl]()
    try {
      obtainMostRetweetedUserId(emptyPostSeq)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // There are no rts
    val noRtPost: StatusImpl = StatusImpl("This is not a RT", new Date(), notExistingId, notExistingId, null)
    val noRTPostSeq: Seq[StatusImpl] = Seq[StatusImpl](noRtPost)
    assertEquals(-1, obtainMostRetweetedUserId(noRTPostSeq))

    // There is only one rt
    val rtStatus: Status = StatusImpl("This is the rt tweet", date, notExistingId, notExistingId, null)
    val rtPost: StatusImpl = StatusImpl("This is a RT", new Date(), existingUserId, notExistingId, rtStatus)
    val rtPostSeq: Seq[StatusImpl] = Seq[StatusImpl](rtPost)

    assertEquals(existingUserId, obtainMostRetweetedUserId(rtPostSeq))

    // There are several rts and there are more from one id than from other ids
    val existingUserIdAlt = 987654321
    val rtPost1: StatusImpl = StatusImpl("Post1 text", new Date(), existingUserId, notExistingId, rtStatus)
    val rtPost2: StatusImpl = StatusImpl("Post2 text", new Date(), existingUserId, notExistingId, rtStatus)
    val rtPost3: StatusImpl = StatusImpl("Post3 text", new Date(), existingUserIdAlt, notExistingId, rtStatus)

    val rtsPostSeq: Seq[StatusImpl] = Seq[StatusImpl](rtPost1, rtPost2, rtPost3)

    assertEquals(existingUserId, obtainMostRetweetedUserId(rtsPostSeq))
  }

  @Test
  def obtainMostRepliedUserIdTest(): Unit = {
    // There are no posts
    val emptyPostSeq: Seq[StatusImpl] = Seq[StatusImpl]()
    try {
      obtainMostRepliedUserId(emptyPostSeq)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // There are no replies
    val noReplyPost: StatusImpl = tweetPost
    val noReplyPostSeq: Seq[StatusImpl] = Seq[StatusImpl](noReplyPost)
    assertEquals(-1, obtainMostRepliedUserId(noReplyPostSeq))

    // There is only one reply

    val reply: StatusImpl = replyPost
    val replyPostSeq: Seq[StatusImpl] = Seq[StatusImpl](reply)

    assertEquals(existingUserId, obtainMostRepliedUserId(replyPostSeq))

    // There are several replies and there are more from one id than from other ids
    val notMostRepliedId: Int = 987654321

    val replyPost1: StatusImpl = replyPost
    val replyPost2: StatusImpl = replyPost
    val replyPost3: StatusImpl = StatusImpl("This is another reply", new Date(), notExistingId, notMostRepliedId, null)
    val repliesPostSeq: Seq[StatusImpl] = Seq[StatusImpl](replyPost1, replyPost2, replyPost3)

    assertEquals(existingUserId, obtainMostRepliedUserId(repliesPostSeq))
  }

  @Test
  def obtainPostActionsProportionTest(): Unit = {
    // no tweets
    val emptyTweets: Seq[StatusImpl] = Seq[StatusImpl]()
    val csvTweets: java.util.ArrayList[String] = new java.util.ArrayList[String]()
    csvTweets.add("Random csv tweet for not being empty list")

    try {
      getMaxFollowedPostActions(emptyTweets, csvTweets)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // no csvTweets
    val tweet: StatusImpl = tweetPost
    val tweets: Seq[StatusImpl] = Seq[StatusImpl](tweet)
    val emptyCSVTweets: java.util.ArrayList[String] = new util.ArrayList[String]()

    try {
      getMaxFollowedPostActions(tweets, emptyCSVTweets)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // no posts
    val rt: StatusImpl = rtPost
    val rtPost2: StatusImpl = rtPost
    val tweetsWithoutPost: Seq[StatusImpl] = Seq[StatusImpl](rt, rtPost2)
    val csvReplyPost1: String = csvReplyPost
    val csvTweetsWithOnlyAReply: java.util.ArrayList[String] = new util.ArrayList[String]()
    csvTweetsWithOnlyAReply.add(csvReplyPost1)

    val postActionProportion = getMaxFollowedPostActions(tweetsWithoutPost, csvTweetsWithOnlyAReply)
    assertEquals(0, postActionProportion)

    // several followed posts in seq. Post actions in Seq are filtered because they are count in csv actions
    val post1: StatusImpl = tweetPost
    val post2: StatusImpl = tweetPost
    val post3: StatusImpl = tweetPost
    val post4: StatusImpl = tweetPost
    val postsSeq: Seq[StatusImpl] = Seq[StatusImpl](post1, post2, post3, post4)

    val csvReply1: String = csvReplyPost
    val csvReply2: String = csvReplyPost
    val csvTweetsOnlyReplies: java.util.ArrayList[String] = new util.ArrayList[String]()
    csvTweetsOnlyReplies.add(csvReply1)
    csvTweetsOnlyReplies.add(csvReply2)

    assertEquals(0, getMaxFollowedPostActions(postsSeq, csvTweetsOnlyReplies))

    // several followed posts in csv
    val post12: StatusImpl = tweetPost
    val post22: StatusImpl = tweetPost
    val postsSeq2: Seq[StatusImpl] = Seq[StatusImpl](post12, post22)

    val numberOfFollowedPosts: Int = 3
    val csvPost1: String = csvTweetPost
    val csvPost2: String = csvTweetPost2
    val csvPost3: String = csvTweetPost3
    val csvPosts: java.util.ArrayList[String] = new util.ArrayList[String]()
    csvPosts.add(csvPost1)
    csvPosts.add(csvPost2)
    csvPosts.add(csvPost3)

    val postActionsProportion = getMaxFollowedPostActions(postsSeq2, csvPosts)
    assertEquals(numberOfFollowedPosts, postActionsProportion)
  }

  @Test
  def obtainMaxActionsPerHourTest(): Unit = {
    // empty seq
    val tweetsSeq: Seq[StatusImpl] = Seq[StatusImpl]()

    val csvTweet: String = csvTweetPost
    val csvTweets: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets.add(csvTweet)

    try {
      obtainMaxActionsPerHour(tweetsSeq, csvTweets)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // empty arraylist
    val tweet: StatusImpl = tweetPost
    val tweetsSeq2: Seq[StatusImpl] = Seq[StatusImpl](tweet)

    val csvTweets2: util.ArrayList[String] = new util.ArrayList[String]()

    try {
      obtainMaxActionsPerHour(tweetsSeq2, csvTweets2)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // only 1 action by date (dont mind post in seq)
    val minActionsInHour: Int = 1
    val tweet1: StatusImpl = tweetPost
    val tweetsSeq3: Seq[StatusImpl] = Seq[StatusImpl](tweet1)

    val csvTweet1: String = csvTweetPost
    val csvTweets3: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets3.add(csvTweet1)

    assertEquals(minActionsInHour, obtainMaxActionsPerHour(tweetsSeq3, csvTweets3))

    // several actions in the same date group
    val severalActionsInHour: Int = 3
    val tweetPost1: StatusImpl = tweetPost
    val tweetPost2: StatusImpl = tweetPost
    val tweetsSeq4: Seq[StatusImpl] = Seq[StatusImpl](tweetPost1, tweetPost2)

    val csvTweetPost1: String = csvTweetPost
    val anotherCSVPost: String = csvTweetPost2
    val anotherOne: String = csvTweetPost3
    val csvTweets4: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets4.add(csvTweetPost1)
    csvTweets4.add(anotherCSVPost)
    csvTweets4.add(anotherOne)

    assertEquals(severalActionsInHour, obtainMaxActionsPerHour(tweetsSeq4, csvTweets4))
  }

  @Test
  def obtainMeanActionsPerHourTest(): Unit = {
    // empty seq
    val tweetsSeq: Seq[StatusImpl] = Seq[StatusImpl]()

    val csvTweet: String = csvTweetPost
    val csvTweets: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets.add(csvTweet)

    try {
      getMeanActionsPerHour(tweetsSeq, csvTweets)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // empty arraylist
    val tweet: StatusImpl = tweetPost
    val tweetsSeq2: Seq[StatusImpl] = Seq[StatusImpl](tweet)

    val csvTweets2: util.ArrayList[String] = new util.ArrayList[String]()

    try {
      getMeanActionsPerHour(tweetsSeq2, csvTweets2)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // 1 action per group
    val oneActionPerGroup: Int = 1
    // This should be filtered and not taking into account
    val tweetPost2: StatusImpl = tweetPost
    val tweetsSeq3: Seq[StatusImpl] = Seq[StatusImpl](tweetPost2)

    val csvTweet2: String = csvTweetPost
    val csvTweets3: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets3.add(csvTweet2)

    assertEquals(oneActionPerGroup, getMeanActionsPerHour(tweetsSeq3, csvTweets3))

    // more than 1 action per group
    val tweetPost3: StatusImpl = tweetPost
    val tweetsSeq4: Seq[StatusImpl] = Seq[StatusImpl](tweetPost3)

    val csvTweet3: String = csvTweetPost
    val csvTweet4: String = csvTweetPost2
    val csvTweets4: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets4.add(csvTweet3)
    csvTweets4.add(csvTweet4)

    val meanActionsPerHour = getMeanActionsPerHour(tweetsSeq4, csvTweets4)
    assertTrue(oneActionPerGroup < meanActionsPerHour)
  }

  @Test
  def obtainRtsDatesTest(): Unit = {
    // No posts in seq
    val tweets: Seq[StatusImpl] = Seq[StatusImpl]()
    try {
      obtainRtsInfo(tweets)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // No rts in seq
    val tweet: StatusImpl = tweetPost
    val tweets2: Seq[StatusImpl] = Seq[StatusImpl](tweet)
    assertEquals(0, obtainRtsInfo(tweets2).length)

    // Rts and post in seq
    val tweet2: StatusImpl = rtPost
    val tweets3: Seq[StatusImpl] = Seq[StatusImpl](tweet, tweet2)

    assertEquals(date.toString + csvSeparator + rtPost.currentUserRtId + csvSeparator + rtId,
    obtainRtsInfo(tweets3).head)

    // Only rts
    val tweet3: StatusImpl = rtPost
    val tweets4: Seq[StatusImpl] = Seq[StatusImpl](tweet2, tweet3)

    assertEquals(tweet2.createdAt.toString + csvSeparator + tweet2.currentUserRtId + csvSeparator + rtId,
      obtainRtsInfo(tweets4).head)
    assertEquals(tweet3.createdAt.toString + csvSeparator + tweet3.currentUserRtId + csvSeparator + rtId,
      obtainRtsInfo(tweets4)(1))
  }


}
