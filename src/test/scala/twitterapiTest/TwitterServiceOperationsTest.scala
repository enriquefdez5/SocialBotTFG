package twitterapiTest

import java.util
import java.util.{Calendar, Date}

import app.twitterAPI.ConfigRun
import model.exceptions.IncorrectSizeListException
import model.StatusImpl
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import twitter4j.Status
import app.twitterAPI.TwitterServiceOperations.{getAPIPattern, getActionsWithWeekSeparator, getAllActionsOrderedByDate, getCSVSeparator, getLastTweetNotReplied, getLastTweetNotRetweeted, getMaxFollowedPostActions, obtainMeanActionsPerHour, getTrainableActions, getTweetText, getTwintSeparator, getWeekSeparator, obtainMaxActionsPerHour, obtainMostRepliedUserId, obtainMostRetweetedUserId, obtainRtsInfo, statusesToStatusImpl}

class TwitterServiceOperationsTest {

  val emptyListExceptionMessage: String = "List can not be empty"
  val csvSeparator: String = ","

  val rtId: String = "3"
  val notExistingId = -1
  val existingUserId: Int = 123456789
  val date: Date = new Date()
  val thisIsAPostText: String = "This is a post"
  val thisIsAReplyText: String = "This is a reply"
  val thisIsARtText: String = "This is a rt post"
  val tweetPost: StatusImpl = StatusImpl(thisIsAPostText, date, notExistingId, notExistingId, null)
  val replyPost: StatusImpl = StatusImpl(thisIsAReplyText, date, notExistingId, existingUserId, null)
  val rtPost: StatusImpl = StatusImpl(thisIsARtText, date, existingUserId, notExistingId, new StatusImpl
  ("anotherPost", date, notExistingId, notExistingId, null))

  val firstTextShard: String = "texto pos 0\ttexto pos 1\ttexto pos " +
    "2\t2021-01-02\t15:30:58\tusername\tusername\t26\t69\t2791" +
    "\t\"This is a normal tweet posted\"\t\t\t"
  val secondTextShard: String = "\t\t1303310580464398336" +
    "\thttps://twitter.com/username/status/1303307843391684609\t\t\t\t\t\t\t\t\t\t\t\t\t\t\treplyUser"
  val csvTweetPost: String = firstTextShard + secondTextShard
  val csvActionPost: String = "10,10,1"
  val csvTweetPost2: String = firstTextShard + "uhuhuh" + secondTextShard
  val csvTweetPost3: String = firstTextShard + "xdxdxd" + secondTextShard


  val csvReplyPost: String = "texto pos 0\ttexto pos 1\ttexto pos " +
    "2\t2021-02-27\t12:33:51\tusername\tusername\t26\t69\t2791" +
    "\t\"buen cochazo se va notando el sueldo de caster\"\t\t\t\t\t1303310580464398336" +
    "\thttps://twitter.com/username/status/1303310580464398336\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
    "[{'screen_name': 'username', 'name': 'Rocket \uD83D\uDE80\uD83C\uDF38', 'id': '160908260'}]"



  @Test
  def getAPIPatternTest(): Unit = {
    assertEquals("EEE MMM dd HH:mm:ss z yyyy", getAPIPattern)
  }
  @Test
  def getWeekSeparatorTest(): Unit = {
    assertEquals(notExistingId.toString, getWeekSeparator)
  }
  @Test
  def getCSVSeparatorTest(): Unit = {
    assertEquals(",", getCSVSeparator)
  }
  @Test
  def getTwintSeparatorTest(): Unit = {
    assertEquals("\t", getTwintSeparator)
  }



  @Test
  def statusesToStatusImplTest(): Unit = {
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
  def getTweetTextTest(): Unit = {
    // Empty list
    val textColumn = 10
    val splitSymbol = "\t"
    val tweetsList: util.ArrayList[String] = new util.ArrayList[String]()
    try {
      getTweetText(tweetsList, textColumn, splitSymbol)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // not empty list
    val tweet1 = "1387470341799297024\t1387469949015318528\t2021-04-28 20:14:42 Romance Daylight " +
      "Time\t2021-04-28\t20:14:42\t+0200\t1076110847947300865\tlec\tLEC\t\tThis is the " +
      "text of the first tweet\ten\t[]\t[]\t['https://pbs" +
      ".twimg.com/media/E0FI2QlX0AgegnA.png']\t5\t5\t635\t[]\t[]\thttps://twitter.com/LEC/status/1387470341799297024\tFalse\t\t1\thttps://pbs.twimg.com/media/E0FI2QlX0AgegnA.png\t\t\t\t\t\t\t[{'screen_name': 'lolesports', 'name': 'LoL Esports', 'id': '614754689'}]"
    tweetsList.add(tweet1)
    assertEquals("This is the text of the first tweet", getTweetText(tweetsList, textColumn, splitSymbol)(0))
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
    val status1: StatusImpl = StatusImpl(thisIsAReplyText, date, notExistingId, existingUserId, null)
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

  private def getConfItem(): ConfigRun = {
    val arraySize = 8
    val args = new Array[String](arraySize)
    val consumerKeyOption = "--consumer-token-key"
    val consumerKey = "5JUmI2nlOFYhvKrWQJrK3DcNC"
    val consumerKeySecretOption = "--consumer-token-key-secret"
    val consumerKeySecret = "XKsFu2p5xEsf4NfSS6JSU7po2MPv3TqwHKw8MKZLpgcDV582zB"
    val accessKeyOption = "--access-token-key"
    val accessKey = "1226181568454066181-1Pq0Xpe82I2amKhmydBmsCSfr8Oqd2"
    val accessKeySecretOption = "--access-token-key-secret"
    val accessKeySecret = "GkLucdYD3LWQK10C7NvywGYFA5PzNiH6jLPRqNPdQLPMm"
    args(0) = consumerKeyOption
    args(1) = consumerKey
    args(2) = consumerKeySecretOption
    args(3) = consumerKeySecret
    args(4) = accessKeyOption
    args(5) = accessKey
    args(6) = accessKeySecretOption
    args(7) = accessKeySecret
    new ConfigRun(args)
  }

  @Test
  def obtainMostRepliedUserIdTest(): Unit = {
    val configRun = getConfItem
    // There are no posts
    val emptyPostSeq: Seq[StatusImpl] = Seq[StatusImpl]()
    try {
      obtainMostRepliedUserId(configRun, emptyPostSeq)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // There are no replies
    val noReplyPost: StatusImpl = tweetPost
    val noReplyPostSeq: Seq[StatusImpl] = Seq[StatusImpl](noReplyPost)
    assertEquals(-1, obtainMostRepliedUserId(configRun, noReplyPostSeq))

    // There is only one reply

    val reply: StatusImpl = replyPost
    val replyPostSeq: Seq[StatusImpl] = Seq[StatusImpl](reply)

    val notExistingUser = 0
    assertEquals(notExistingUser, obtainMostRepliedUserId(configRun, replyPostSeq))

    // There are several replies and there are more from one id than from other ids
    val notMostRepliedId: Int = 1234567890

    val replyPost1: StatusImpl = replyPost
    val replyPost2: StatusImpl = replyPost
    val replyPost3: StatusImpl = StatusImpl("This is another reply", new Date(), notExistingId, notMostRepliedId, null)
    val repliesPostSeq: Seq[StatusImpl] = Seq[StatusImpl](replyPost1, replyPost2, replyPost3)

    val existingUserId = 18934481
    assertEquals(existingUserId, obtainMostRepliedUserId(configRun, repliesPostSeq))
  }

  @Test
  def getMaxFollowedPostActinosTest(): Unit = {
    // no csvTweets
    val emptyCSVTweets: java.util.ArrayList[String] = new util.ArrayList[String]()

    try {
      getMaxFollowedPostActions(emptyCSVTweets)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // no posts
    val csvReplyPost1: String = csvReplyPost
    val csvTweetsWithOnlyAReply: java.util.ArrayList[String] = new util.ArrayList[String]()
    csvTweetsWithOnlyAReply.add(csvReplyPost1)

    assertEquals(0, getMaxFollowedPostActions(csvTweetsWithOnlyAReply))

    // several followed posts
    val numberOfFollowedPosts: Int = 3
    val csvPost1: String = csvActionPost
//    val csvPost1: String = csvTweetPost
    val csvPost2: String = csvActionPost
//    val csvPost2: String = csvTweetPost2
    val csvPost3: String = csvActionPost
//    val csvPost3: String = csvTweetPost3
    val csvPosts: java.util.ArrayList[String] = new util.ArrayList[String]()
    csvPosts.add(csvPost1)
    csvPosts.add(csvPost2)
    csvPosts.add(csvPost3)

    assertEquals(numberOfFollowedPosts, getMaxFollowedPostActions(csvPosts))
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
  def getMeanActionsPerHourTest(): Unit = {
    // empty arraylist
    val tweet: StatusImpl = tweetPost
    val tweetsSeq2: Seq[StatusImpl] = Seq[StatusImpl](tweet)

    val csvTweets2: util.ArrayList[String] = new util.ArrayList[String]()

    try {
      obtainMeanActionsPerHour(csvTweets2)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // 1 action per group
    val oneActionPerGroup: Int = 1
//    val csvTweet2: String = csvTweetPost
    val csvTweet2: String = csvActionPost
    val csvTweets3: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets3.add(csvTweet2)

    assertEquals(oneActionPerGroup, obtainMeanActionsPerHour(csvTweets3))

    // more than 1 action per group
    val csvTweet3: String = csvActionPost
    val csvTweet4: String = csvActionPost
    val csvTweets4: util.ArrayList[String] = new util.ArrayList[String]()
    csvTweets4.add(csvTweet3)
    csvTweets4.add(csvTweet4)

    val meanActionsPerHour = obtainMeanActionsPerHour(csvTweets4)
    assertTrue(oneActionPerGroup < meanActionsPerHour)
  }

  @Test
  def obtainRtsInfoTest(): Unit = {
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

  @Test
  def getActionsWithWeekSeparatorTest(): Unit = {
    // Empty list
    val emptySeq: Seq[String] = Seq[String]()
    try {
      getActionsWithWeekSeparator(emptySeq)
    }
    catch {
      case exception: IncorrectSizeListException => { assertEquals(emptyListExceptionMessage, exception.msg )}
    }


    // Same week posts
    val sameWeek = new Date().toString
    val sameWeekActionsSeq: Seq[String] = Seq[String](sameWeek + ",algomas", sameWeek + ", otra cosilla")
    assertEquals(false, getActionsWithWeekSeparator(sameWeekActionsSeq).toString.contains(notExistingId.toString))

    // Diff week posts
    val calendar = Calendar.getInstance()
    calendar.setTime(tweetPost.createdAtDate)
    val minimumDayOfWeek = calendar.getMinimum(Calendar.DAY_OF_WEEK)
    calendar.set(Calendar.DAY_OF_WEEK, minimumDayOfWeek)
    val firstDay = calendar.getTime.toString

    calendar.setTime(tweetPost.createdAtDate)
    calendar.set(Calendar.DAY_OF_WEEK, minimumDayOfWeek + 1)
    val secondDay = calendar.getTime.toString
    val diffWeeksActinosSeq: Seq[String] = Seq[String](secondDay + ", primer valor", firstDay + ", segundo " +
      "valor")
    assertEquals(true, getActionsWithWeekSeparator(diffWeeksActinosSeq).toString.contains(notExistingId.toString))
  }

  @Test
  def getAllActionsOrderedByDatesTest(): Unit = {
    // empty seqs
    val emptySeq: Seq[StatusImpl] = Seq[StatusImpl]()
    val emptyList: util.ArrayList[String] = new util.ArrayList[String]()
    try {
      getAllActionsOrderedByDate(emptySeq, emptyList)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg )
    }

    // empty arrayList
    val notEmptySeq: Seq[StatusImpl] = Seq[StatusImpl](tweetPost)
    try {
      getAllActionsOrderedByDate(notEmptySeq, emptyList)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg )
    }

    // one element (posts in seq are not count)
    val notEmptyList: util.ArrayList[String] = new util.ArrayList[String]()
    notEmptyList.add(csvTweetPost)
    assertEquals(1, getAllActionsOrderedByDate(notEmptySeq, notEmptyList).length)
    val csvDateAndType = "Sat Jan 02 15:30:58 CET 2021,,1\n"
    assertEquals(csvDateAndType, getAllActionsOrderedByDate(notEmptySeq, notEmptyList).head)

    // several elements
    val seqWithPost: Seq[StatusImpl] = Seq[StatusImpl](rtPost)
    val actionsOrdered = getAllActionsOrderedByDate(seqWithPost, notEmptyList)
    assertEquals(2, actionsOrdered.length)
    assertEquals(csvDateAndType, actionsOrdered.head)
  }

  @Test
  def getTrainableActionsTest(): Unit = {
    // Empty string
    val emptySeq: Seq[String] = Seq[String]()
    try {
      getTrainableActions(emptySeq)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // Without -1
    val action = "Sat Jan 02 15:30:58 CET 2021,,1\n"
    val actionResult = "7,15,1\n"
    val withoutMinus1: Seq[String] = Seq[String](action)
    assertEquals(actionResult, getTra0inableActions(withoutMinus1).head)

    // With -1
    val action2 = "Mon Jan 04 15:30:30 CET 2021,,1\n"
    val separator = notExistingId.toString + "\n"
    val actionResult2 = "2,15,1\n"
    val withSeparator: Seq[String] = Seq[String](action, separator, action2)
    assertEquals(actionResult, getTrainableActions(withSeparator).head)
    assertEquals(actionResult2, getTrainableActions(withSeparator)(2))
  }


}
