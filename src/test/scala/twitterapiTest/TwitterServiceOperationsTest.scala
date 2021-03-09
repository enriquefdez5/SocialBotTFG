package test.scala.twitterapiTest

import java.util
import java.util.Date

import model.exceptions.IncorrectSizeListException
import model.{Post, StatusImpl}
import org.junit.Assert
import org.junit.jupiter.api.Test
import twitter4j.Status
import twitterapi.TwitterServiceOperations._

object TwitterServiceOperationsTest {

  val emptyListExceptionMessage: String = "List can not be empty"
  val csvSeparator: String = ","

  val rtId: String = "3"
  val notExistingId = -1
  val existingUserId: Int = 123456789
  val date: Date = new Date()
  val tweetPost: Post = Post("This is a post", date, null, notExistingId, notExistingId)
  val replyPost: Post = Post("This is a reply", date, null, notExistingId, existingUserId)
  val rtPost: Post = Post("This is a rt post", date,
    new StatusImpl("anotherPost", date, null, notExistingId, notExistingId),
    existingUserId, notExistingId)

  val csvTweetPost: String = "2021-01-02 15:30:58,username,,65,728,8380,\"This is a normal tweet posted\",,,," +
    "1303307843391684609,https://twitter.com/username/status/1303307843391684609"
  val csvReplyPost: String = "2021-02-27 12:33:51,anotherUsername,username,26,69,2791,\"This is a reply tweet\",,,," +
    "1303310580464398336,https://twitter.com/username/status/1303310580464398336"



  @Test
  def statusesToPostTest(): Unit = {
    // empty seq
    val emptyStatusSeq: Seq[Status] = Seq[Status]()
    try {
      statusesToPosts(emptyStatusSeq)
      Assert.assertEquals(emptyStatusSeq.length, statusesToPosts(emptyStatusSeq).length)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }


    // 2 status to posts
    val status1: StatusImpl = new StatusImpl("Status 1 text", date, null, existingUserId, notExistingId)
    val status2: StatusImpl = new StatusImpl("Status 1 text", date, null, existingUserId, notExistingId)
    val statusSeq: Seq[Status] = Seq[Status](status1, status2)
    val postSeq: Seq[Post] = statusesToPosts(statusSeq)
    Assert.assertEquals(statusSeq.length, postSeq.length)
    Assert.assertEquals(statusSeq.head.getText, postSeq.head.text)
    Assert.assertEquals(statusSeq.head.getCreatedAt, postSeq.head.createdAt)
  }

  @Test
  def getLastTweetNotRetweetedTest(): Unit = {
    // no tweets on seq
    val postSeq: Seq[Status] = Seq[Status]()
    try {
      getLastTweetNotRetweeted(postSeq, 0)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // rt on seq
    val status1: Status = new StatusImpl("This is a rt", date,
      new StatusImpl("anotherPost", date, null, notExistingId, notExistingId),
      existingUserId, notExistingId)
    val postSeq2: Seq[Status] = Seq[Status](status1)
    Assert.assertEquals(null, getLastTweetNotRetweeted(postSeq2, 0))


    // no rt on seq
    val status2: Status = new StatusImpl("This is a post", date, null, notExistingId, notExistingId)
    val status3: Status = new StatusImpl("This is a post", date, null, notExistingId, notExistingId)
    val postSeq3: Seq[Status] = Seq[Status](status2, status3)
    val lastTweetNotRetweeted = getLastTweetNotRetweeted(postSeq3, 0)
    Assert.assertEquals(status2.getText, lastTweetNotRetweeted.getText)
    Assert.assertEquals(status2.getCreatedAt, lastTweetNotRetweeted.getCreatedAt)
  }

  @Test
  def getLastTweetNotRepliedTest(): Unit = {
    // no tweets on seq
    val postSeq1: Seq[Status] = Seq[Status]()
    try {
      getLastTweetNotReplied(postSeq1, 0)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // a reply on seq
    val status1: StatusImpl = new StatusImpl("This is a reply", date, null, notExistingId, existingUserId)
    val postSeq2: Seq[Status] = Seq[Status](status1)
    Assert.assertEquals(null, getLastTweetNotReplied(postSeq2, 0))

    // no replies on seq
    val status2: StatusImpl = new StatusImpl("This is not a reply", date, null, notExistingId, notExistingId)
    val status3: StatusImpl = new StatusImpl("This is not a reply", date, null, notExistingId, notExistingId)
    val postSeq3: Seq[Status] = Seq[Status](status2, status3)
    val lastTweetNotReplied = getLastTweetNotReplied(postSeq3, 0)
    Assert.assertEquals(status2.getText, lastTweetNotReplied.getText)
    Assert.assertEquals(status2.getCreatedAt, lastTweetNotReplied.getCreatedAt)
  }

  @Test
  def obtainMostRetweetedUserIdTest(): Unit = {
    // There are no posts
    val emptyPostSeq: Seq[Post] = Seq[Post]()
    try {
      obtainMostRetweetedUserId(emptyPostSeq)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // There are no rts
    val noRtPost: Post = Post("This is not a RT", new Date(), null, notExistingId, notExistingId)
    val noRTPostSeq: Seq[Post] = Seq[Post](noRtPost)
    Assert.assertEquals(-1, obtainMostRetweetedUserId(noRTPostSeq))

    // There is only one rt
    val rtStatus = new StatusImpl("This is the rt tweet", date, null, notExistingId, notExistingId)
    val rtPost: Post = Post("This is a RT", new Date(), rtStatus, existingUserId, notExistingId)
    val rtPostSeq: Seq[Post] = Seq[Post](rtPost)

    Assert.assertEquals(existingUserId, obtainMostRetweetedUserId(rtPostSeq))

    // There are several rts and there are more from one id than from other ids
    val existingUserIdAlt = 987654321
    val rtPost1: Post = Post("Post1 text", new Date(), rtStatus, existingUserId, notExistingId)
    val rtPost2: Post = Post("Post2 text", new Date(), rtStatus, existingUserId, notExistingId)
    val rtPost3: Post = Post("Post3 text", new Date(), rtStatus, existingUserIdAlt, notExistingId)

    val rtsPostSeq: Seq[Post] = Seq[Post](rtPost1, rtPost2, rtPost3)

    Assert.assertEquals(existingUserId, obtainMostRetweetedUserId(rtsPostSeq))
  }

  @Test
  def obtainMostRepliedUserIdTest(): Unit = {
    // There are no posts
    val emptyPostSeq: Seq[Post] = Seq[Post]()
    try {
      obtainMostRepliedUserId(emptyPostSeq)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // There are no replies
    val noReplyPost: Post = tweetPost
    val noReplyPostSeq: Seq[Post] = Seq[Post](noReplyPost)
    Assert.assertEquals(-1, obtainMostRepliedUserId(noReplyPostSeq))

    // There is only one reply

    val reply: Post = replyPost
    val replyPostSeq: Seq[Post] = Seq[Post](reply)

    Assert.assertEquals(existingUserId, obtainMostRepliedUserId(replyPostSeq))

    // There are several replies and there are more from one id than from other ids
    val notMostRepliedId: Int = 987654321

    val replyPost1: Post = replyPost
    val replyPost2: Post = replyPost
    val replyPost3: Post = Post("This is another reply", new Date(), null, notExistingId, notMostRepliedId)
    val repliesPostSeq: Seq[Post] = Seq[Post](replyPost1, replyPost2, replyPost3)

    Assert.assertEquals(existingUserId, obtainMostRepliedUserId(repliesPostSeq))
  }

  @Test
  def obtainPostActionsProportionTest(): Unit = {
    // no tweets
    val emptyTweets: Seq[Post] = Seq[Post]()
    val csvTweets: java.util.ArrayList[String] = new java.util.ArrayList[String]()
    csvTweets.add("Random csv tweet for not being empty list")

    try {
      obtainPostActionsProportion(emptyTweets, csvTweets)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // no csvTweets
    val tweet: Post = tweetPost
    val tweets: Seq[Post] = Seq[Post](tweet)
    val emptyCSVTweets: java.util.ArrayList[String] = new util.ArrayList[String]()

    try {
      obtainPostActionsProportion(tweets, emptyCSVTweets)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // no posts
    val rt: Post = rtPost
    val rtPost2: Post = rtPost
    val tweetsWithoutPost: Seq[Post] = Seq[Post](rt, rtPost2)
    val csvReplyPost1: String = csvReplyPost
//    "2021-02-27 12:33:51,anotherUsername,username,26,69,2791,\"buen cochazo se va " +
//      "notando el sueldo de caster\",,,,1303310580464398336,https://twitter.com/IbaiLlanos/status/1303310580464398336"
    val csvTweetsWithOnlyAReply: java.util.ArrayList[String] = new util.ArrayList[String]()
    csvTweetsWithOnlyAReply.add(csvReplyPost1)

    Assert.assertEquals(0, obtainPostActionsProportion(tweetsWithoutPost, csvTweetsWithOnlyAReply))

    // several followed posts in seq. Post actions in Seq are filtered because they are count in csv actions
    val post1: Post = tweetPost
    val post2: Post = tweetPost
    val post3: Post = tweetPost
    val post4: Post = tweetPost
    val postsSeq: Seq[Post] = Seq[Post](post1, post2, post3, post4)

    val csvReply1: String = csvReplyPost
//      "2021-02-27 12:33:51,anotherUsername,username,26,69,2791,\"buen cochazo se va " +
//      "notando el sueldo de caster\",,,,1303310580464398336,https://twitter.com/Username/status/1303310580464398336"
    val csvReply2: String = csvReplyPost
//"2021-02-27 16:12:00,anotherOne,username,26,69,2791,\"toma texto crack\",,,," +
//      "12312314123131,https://twitter.com/Username/status/12312314123131"
    val csvTweetsOnlyReplies: java.util.ArrayList[String] = new util.ArrayList[String]()
    csvTweetsOnlyReplies.add(csvReply1)
    csvTweetsOnlyReplies.add(csvReply2)

    Assert.assertEquals(0, obtainPostActionsProportion(postsSeq, csvTweetsOnlyReplies))

    // several followed posts in csv
    val post12: Post = tweetPost
    val post22: Post = tweetPost
    val postsSeq2: Seq[Post] = Seq[Post](post12, post22)

    val numberOfFollowedPosts: Int = 3
    val csvPost1: String = csvTweetPost
    val csvPost2: String = csvTweetPost
    val csvPost3: String = csvTweetPost
    val csvPosts: java.util.ArrayList[String] = new util.ArrayList[String]()
    csvPosts.add(csvPost1)
    csvPosts.add(csvPost2)
    csvPosts.add(csvPost3)

    Assert.assertEquals(numberOfFollowedPosts, obtainPostActionsProportion(postsSeq2, csvPosts))
  }

  @Test
  def obtainMaxActionsPerHourTest(): Unit = {
    // empty seq
    val tweetsSeq: Seq[Post] = Seq[Post]()

    val csvTweet: String = csvTweetPost
    val csvTweets: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets.add(csvTweet)

    try {
      obtainMaxActionsPerHour(tweetsSeq, csvTweets)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // empty arraylist
    val tweet: Post = tweetPost
    val tweetsSeq2: Seq[Post] = Seq[Post](tweet)

    val csvTweets2: util.ArrayList[String] = new util.ArrayList[String]()

    try {
      obtainMaxActionsPerHour(tweetsSeq2, csvTweets2)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // only 1 action by date (dont mind post in seq)
    val minActionsInHour: Int = 1
    val tweet1: Post = tweetPost
    val tweetsSeq3: Seq[Post] = Seq[Post](tweet1)

    val csvTweet1: String = csvTweetPost
    val csvTweets3: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets3.add(csvTweet1)

    Assert.assertEquals(minActionsInHour, obtainMaxActionsPerHour(tweetsSeq3, csvTweets3))

    // several actions in the same date group
    val severalActionsInHour: Int = 3
    val tweetPost1: Post = tweetPost
    val tweetPost2: Post = tweetPost
    val tweetsSeq4: Seq[Post] = Seq[Post](tweetPost1, tweetPost2)

    val csvTweetPost1: String = csvTweetPost
    val csvTweetPost2: String = csvTweetPost
    val csvTweets4: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets4.add(csvTweetPost1)
    csvTweets4.add(csvTweetPost1)
    csvTweets4.add(csvTweetPost2)

    Assert.assertEquals(severalActionsInHour, obtainMaxActionsPerHour(tweetsSeq4, csvTweets4))
  }

  @Test
  def obtainMeanActionsPerHourTest(): Unit = {
    // empty seq
    val tweetsSeq: Seq[Post] = Seq[Post]()

    val csvTweet: String = csvTweetPost
    val csvTweets: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets.add(csvTweet)

    try {
      obtainMeanActionsPerHour(tweetsSeq, csvTweets)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // empty arraylist
    val tweet: Post = tweetPost
    val tweetsSeq2: Seq[Post] = Seq[Post](tweet)

    val csvTweets2: util.ArrayList[String] = new util.ArrayList[String]()

    try {
      obtainMeanActionsPerHour(tweetsSeq2, csvTweets2)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // 1 action per group
    val oneActionPerGroup: Int = 1
    // This should be filtered and not taking into account
    val tweetPost2: Post = tweetPost
    val tweetsSeq3: Seq[Post] = Seq[Post](tweetPost2)

    val csvTweet2: String = csvTweetPost
    val csvTweets3: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets3.add(csvTweet2)

    Assert.assertEquals(oneActionPerGroup, obtainMeanActionsPerHour(tweetsSeq3, csvTweets3))

    // more than 1 action per group
    val tweetPost3: Post = tweetPost
    val tweetsSeq4: Seq[Post] = Seq[Post](tweetPost3)

    val csvTweet3: String = csvTweetPost
    val csvTweet4: String = csvTweetPost
    val csvTweets4: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets4.add(csvTweet3)
    csvTweets4.add(csvTweet4)

    Assert.assertTrue(oneActionPerGroup < obtainMeanActionsPerHour(tweetsSeq4, csvTweets4))
  }

  @Test
  def obtainRtsDatesTest(): Unit = {
    // No posts in seq
    val tweets: Seq[Post] = Seq[Post]()
    try {
      obtainRtsInfo(tweets)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // No rts in seq
    val tweet: Post = tweetPost
    val tweets2: Seq[Post] = Seq[Post](tweet)
    Assert.assertEquals(0, obtainRtsInfo(tweets2).length)

    // Rts and post in seq
    val tweet2: Post = rtPost
    val tweets3: Seq[Post] = Seq[Post](tweet, tweet2)

    Assert.assertEquals(date.toString + csvSeparator + rtPost.retweetedStatusUserId + csvSeparator + rtId,
    obtainRtsInfo(tweets3).head)

    // Only rts
    val tweet3: Post = rtPost
    val tweets4: Seq[Post] = Seq[Post](tweet2, tweet3)

    Assert.assertEquals(tweet2.createdAt.toString + csvSeparator + tweet2.retweetedStatusUserId + csvSeparator + rtId,
      obtainRtsInfo(tweets4).head)
    Assert.assertEquals(tweet3.createdAt.toString + csvSeparator + tweet3.retweetedStatusUserId + csvSeparator + rtId,
      obtainRtsInfo(tweets4)(1))
  }


}
