package app.twitterAPI

import java.util
import java.util.Random

import model.StatusImpl
import org.apache.logging.log4j.scala.Logging
import twitter4j.{Status, TwitterException}
import org.json.JSONArray
import utilities.dates.DatesUtilTrait
import utilities.validations.ValidationsUtilTrait

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.mutable

/** Object that contains the functions and operations needed to work with Twitter gathered data without accessing the
 *  Twitter API.
 */
object TwitterServiceOperations extends Logging with TwitterClientTrait with ValidationsUtilTrait with DatesUtilTrait {

  val apiPattern: String = "EEE MMM dd HH:mm:ss z yyyy"
  val weekSeparator: String = "-1"
  val csvSeparator: String = ","
  val twintSeparator: String = "\t"

  /**
   * @return API tweets date pattern.
   */
  def getAPIPattern: String = {
    apiPattern
  }

  /**
   * @return Type and date action file week separator.
   */
  def getWeekSeparator: String = {
    weekSeparator
  }

  /**
   * @return Csv file string separator.
   */
  def getCSVSeparator: String = {
    csvSeparator
  }

  /**
   * @return Twint csv file string separator.
   */
  def getTwintSeparator: String = {
    twintSeparator
  }

  /** Transform Twitter API tweets objects into system tweets object.
   *
   * @param tweets Sequence of tweets objects from Twitter API.
   * @return Sequence of tweets objects from system.
   */
  def statusesToStatusImpl(tweets: Seq[Status]): Seq[StatusImpl] = {
    checkNotEmptySeq(tweets)

    val calendar = getCalendarInstance
    tweets.map(it => {
      calendar.setTime(it.getCreatedAt)
      if (it.getRetweetedStatus != null) {
        StatusImpl(it.getText, calendar.getTime, it.getRetweetedStatus.getUser.getId, -1, it.getRetweetedStatus)
      }
      else {
        StatusImpl(it.getText, calendar.getTime, -1, it.getInReplyToStatusId, it.getRetweetedStatus)
      }
    })
  }

  /** Get tweet text content from a tweet.
   *
   * @param tweets Tweets to get text from.
   * @param textColumn. Column where text content is.
   * @param splitSymbol. String symbol to split tweet data and access tweet text content.
   * @return Sequence of tweets text content.
   */
  def getTweetText(tweets: java.util.ArrayList[String], textColumn: Int, splitSymbol: String)
  : mutable.Buffer[String] = {
    checkNotEmptySeq(tweets)
    tweets.map({ tweet =>
      try {
        tweet.split(splitSymbol)(textColumn).replace("\"", "")
      }
      catch {
        case _: Exception =>
          logger.error("Tweet could not be parsed: " + tweet)
          tweet
      }
    })
  }

  /** Get last tweet not retweeted.
   *
   * @param tweets Tweets gathered from Twitter API.
   * @param idx Index to iterate over tweets.
   * @return Last tweet not retweeted.
   */
  @tailrec
  def getLastTweetNotRetweeted(tweets: Seq[Status], idx: Int): Status = {
    checkNotEmptySeq(tweets)
    checkNotNegativeInt(idx)

    if (idx < tweets.length) {
      if (tweets(idx).isRetweet) {
        getLastTweetNotRetweeted(tweets, idx + 1)
      }
      else {
        tweets(idx)
      }
    }
    else {
      null
    }
  }

  /** Get last tweet not replied.
   *
   * @param tweets Tweets gathered from Twitter API.
   * @param idx Index to iterate over tweets.
   * @return Last tweet not replied.
   */
  @tailrec
  def getLastTweetNotReplied(tweets: Seq[Status], idx: Int): Status = {
    checkNotEmptySeq(tweets)
    checkNotNegativeInt(idx)

    if (idx < tweets.length) {
      if (tweets(idx).getInReplyToStatusId != -1) {
        getLastTweetNotReplied(tweets, idx + 1)
      }
      else {
        tweets(idx)
      }
    }
    else {
      null
    }
  }

  /** Get id of most retweeted user.
   *
   * @param tweets Tweets obtained from Twitter API.
   * @return Most retweeted user identifier.
   */
  def obtainMostRetweetedUserId(tweets: Seq[StatusImpl]): Long = {
    checkNotEmptySeq(tweets)

    val onlyRts = tweets.filter(it => {
      isRetweet(it.rtStatus)
    })

    if (onlyRts.nonEmpty) {
      val onlyRetweetedGroupedByUserId = onlyRts.groupBy(it => {
        it.currentUserRtId
      })

      getMostInteractedUser(onlyRetweetedGroupedByUserId)
    }
    else { -1 }
  }

  /** Get id of most replied user.
   *
   * @param tweets Tweets obtained from Twitter API.
   * @return Most replied user identifier.
   */
  def obtainMostRepliedUserId(conf: ConfigRun, tweets: Seq[StatusImpl]): Long = {
    checkNotEmptySeq(tweets)

    val twitter = getTwitterClient(conf)

    val onlyReplied = tweets.filter(it => {
      isReplied(it.getInReplyToUserId)
    })

    if (onlyReplied.nonEmpty) {
      val onlyRepliedGroupedByUserId = onlyReplied.groupBy(it => {
        try {
          val statusid = twitter.showStatus(it.getInReplyToStatusId).getUser.getId
          val user = twitter.showUser(statusid)
          user.getId
        }
        catch {
          case _: TwitterException => -1
        }
      })

      getMostInteractedUser(onlyRepliedGroupedByUserId)
    }
    else { -1 }
  }

  /** Get the maximum number of followed post type actions
   *
   * @param tweets Type and date action information
   * @return Maximum number of followed post type actions
   */
  def getMaxFollowedPostActions(tweets: util.ArrayList[String]): Int = {
    val actions = tweets.map(tweet => {
      if (tweet != getWeekSeparator) {
        tweet.split(getCSVSeparator)(2)
      }
    })
    val postCount = 0
    val maxCount = 0
    val idx = 0
    getMaxFollowedPostActionsCount(actions, postCount, maxCount, idx)
  }


  /** Get maximum number of actions in an hour.
   *
   * @param tweets Tweets obtained from Twitter API.
   * @param csvTweets Tweets obtained from Twint.
   * @return Maximum number of actions in an hour.
   */
  def obtainMaxActionsPerHour(tweets: Seq[StatusImpl], csvTweets: util.ArrayList[String]): Int = {
    checkNotEmptySeq(tweets)
    checkNotEmptySeq(csvTweets)

    val dayOfYearAndHourMap = groupTwitterActionsByDates(tweets, csvTweets)
    obtainMaxNumberOfActions(dayOfYearAndHourMap)
  }

  /** Get mean actions per hour
   *
   * @param tweets Type and date action information
   * @return Computed mean value
   */
  def obtainMeanActionsPerHour(tweets: util.ArrayList[String]): Int = {
    checkNotEmptySeq(tweets)

    val tweetsArray: Array[String] = tweets.toString.split(getWeekSeparator)
    val tweetsArrayWithoutInitAndEndSymbol = tweetsArray.map(tweet => tweet.replace("[", ""))
                                                        .map(tweet => tweet.replace("]", ""))

    var numberOfActions = 0
    var numberOfGroups = 0
    tweetsArrayWithoutInitAndEndSymbol.foreach(week => {
      val grouped = week.split(", ").groupBy( action => {
        if (action != getWeekSeparator && !action.isBlank) {
          val splitAction = action.split(getCSVSeparator)
          splitAction(0) + getCSVSeparator + splitAction(1)
        }
      })
      grouped.foreach(group => {
        numberOfActions += group._2.length
        numberOfGroups += 1
      })
    })
    numberOfActions / numberOfGroups
  }

  /** Get retweet data from a sequence of tweets.
   *
   * @param tweets Tweets obtained from Twitter API.
   * @return Sequence of retweet data.
   */
  def obtainRtsInfo(tweets: Seq[StatusImpl]): Seq[String] = {
    checkNotEmptySeq(tweets)

    val onlyRTs = tweets.filter(tweet => {
      isRetweet(tweet.rtStatus)
    })

    onlyRTs.map(tweet => {
      tweet.createdAtDate.toString + getCSVSeparator + tweet.currentUserRtId + getCSVSeparator + "3"
    })
  }

  /** Separate tweet of different weeks.
   *
   * @param actionTrainingTweets Tweet as string data.
   * @return sequence of tweets string week separated.
   */
  def getActionsWithWeekSeparator(actionTrainingTweets: Seq[String]): Seq[String] = {
    checkNotEmptySeq(actionTrainingTweets)
    var lastDay = 0
    val calendar = getCalendarInstance
    val parser = getSimpleDateFormat(getAPIPattern)

    val newActionTrainableSeq: util.ArrayList[String] = new util.ArrayList[String]()
    actionTrainingTweets.map(action => {
      val splitAction = action.split(getCSVSeparator)
      calendar.setTime(parser.parse(splitAction(0)))
      val day = getCalendarDay(calendar)
      if (day < lastDay) {
        lastDay = day
        newActionTrainableSeq.add("-1\n")
      }
      else {
        lastDay = day
      }
      newActionTrainableSeq.add(action)
    })
    newActionTrainableSeq
  }

  /** Order tweets by date.
   *
   * @param tweets Tweets gathered from Twitter API.
   * @param csvTweets Tweets gathered from Twint.
   * @return Tweets ordered by date.
   */
  def getAllActionsOrderedByDate(tweets: Seq[StatusImpl], csvTweets: util.ArrayList[String])
  : Seq[String] = {
    checkNotEmptySeq(tweets)
    checkNotEmptySeq(csvTweets)

    val distinctCSVTweets = csvTweets.distinct
    val csvDates = getCSVDates(distinctCSVTweets)

    val rts = obtainRtsInfo(tweets)

    val dates = csvDates.map( csvDate => {
      if (csvDate.split(getCSVSeparator).length > 1) {
        csvDate + ",2"
      }
      else {
        csvDate + ",1"
      }
    })

    rts.foreach(rt => {
      dates.add(rt)
    })

    orderDates(dates)
  }

  /** Transform string tweet data into trainable string data.
   *
   * @param actions Tweet as string data.
   * @return a trainable sequence of string.
   */
  def getTrainableActions(actions: Seq[String]): Seq[String] = {
    checkNotEmptySeq(actions)

    val calendar = getCalendarInstance
    val parser = getSimpleDateFormat(getAPIPattern)
    actions.map(action => {
      if (action != "-1\n") {
        val splitAction = action.split(getCSVSeparator)
        calendar.setTime(parser.parse(splitAction(0)))
        getCalendarDay(calendar) + getCSVSeparator + getCalendarHour(calendar) + getCSVSeparator + splitAction(2)
      }
      else { action }
    })
  }


  private def getMostInteractedUser(groupedTweets: Map[Long, Seq[StatusImpl]]): Long = {
    val maxSize: Int = 0
    val mostInteractedUser: Long = 0
    val idx = 0
    val keys = groupedTweets.keys.toList
    getMostInteractedUserLoop(groupedTweets, maxSize, mostInteractedUser, keys, idx)
  }

  @tailrec
  private def getMostInteractedUserLoop(groupedTweets: Map[Long, Seq[StatusImpl]], maxSize: Int,
                                        mostInteractedUserId: Long,
                                        keys: List[Long], idx: Int): Long = {
    checkNotNegativeInt(maxSize)
    checkNotNegativeLong(mostInteractedUserId)
    checkNotEmptySeq(keys)
    checkNotNegativeInt(idx)

    if (idx < keys.length) {
      if (keys(idx) != -1) {
        val size = groupedTweets(keys(idx)).size
        if (size > maxSize) {
          getMostInteractedUserLoop(groupedTweets, groupedTweets(keys(idx)).size, keys(idx), keys, idx + 1)
        }
        else if (groupedTweets.get(keys(idx)).size == maxSize) {
          val randomBound = 100
          val chance = 60
          if (new Random().nextInt(randomBound) <= chance ) {
            getMostInteractedUserLoop(groupedTweets, groupedTweets.get(keys(idx)).size, keys(idx), keys, idx + 1)
          }
          else {
            getMostInteractedUserLoop(groupedTweets, maxSize, mostInteractedUserId, keys, idx + 1)
          }
        }
        else {
          getMostInteractedUserLoop(groupedTweets, maxSize, mostInteractedUserId, keys, idx + 1)
        }
      }
      else {
        getMostInteractedUserLoop(groupedTweets, maxSize, mostInteractedUserId, keys, idx + 1)
      }
    }
    else { mostInteractedUserId }
  }

  @tailrec
  private def getMaxFollowedPostActionsCount(actions: mutable.Buffer[Any], postCount: Int, maxCount: Int, idx: Int)
  : Int = {
    if (idx < actions.length) {
      if (actions.get(idx) == "1") {
        if (postCount >= maxCount) {
          getMaxFollowedPostActionsCount(actions, postCount + 1, postCount + 1, idx + 1)
        }
        else {
          getMaxFollowedPostActionsCount(actions, postCount + 1, maxCount, idx + 1)
        }
      }
      else {
        getMaxFollowedPostActionsCount(actions, 0, maxCount, idx + 1)
      }
    }
    else {
      maxCount
    }
  }

  private def obtainMaxNumberOfActions(dayOfYearAndHourMap: Iterable[Map[Int, Seq[String]]]): Int = {
    dayOfYearAndHourMap.maxBy(mapGroup => {
      mapGroup.values.size
    }).head._2.size
  }

  private def isRetweet(retweetStatus: Status, rtUserId: Long = 0): Boolean = {
    retweetStatus != null && rtUserId != -1
  }

  private def isReplied(replyId: Long): Boolean = {
    replyId != -1
  }


  private def getMentionName(str: String): String = {
    if (str != "") {
      try {
        val jsonArray = new JSONArray(str.stripMargin)
        if (jsonArray.length() > 0) {
          jsonArray.getJSONObject(0).getString("screen_name")
        }
        else {
          ""
        }
      }
      catch {
        case _: Exception =>
          ""
      }
    }
    else {
      ""
    }
  }

  private def orderDates(dates: mutable.Buffer[String]): mutable.Buffer[String] = {
    val simpleDateFormatAPI = getSimpleDateFormat(getAPIPattern)

    val calendarToOrder = getCalendarInstance
    val orderedDates = dates.sortBy(tweet => {
      val stringToDate = tweet.split(getCSVSeparator)(0)
      calendarToOrder.setTime(simpleDateFormatAPI.parse(stringToDate))
      calendarToOrder.getTime
    })

    orderedDates.map(_ + "\n")
  }

  private def getCSVDates(distinctCSVTweets: mutable.Buffer[String]): mutable.Buffer[String] = {
    val csvPattern = "yyyy-MM-dd HH:mm:ss"
    val simpleDateFormatCSV = getSimpleDateFormat(csvPattern)

    val dateColumn = 3
    val timeColumn = 4
    val mentionColumn = 31

    val calendar = getCalendarInstance

    distinctCSVTweets.map(tweet => {
      try {
        val split = tweet.split(getTwintSeparator)
        calendar.setTime(simpleDateFormatCSV.parse(split(dateColumn) + " " + split(timeColumn)))
        calendar.getTime.toString + getCSVSeparator + getMentionName(split(mentionColumn))
      }
      catch {
        case _: Exception =>
          calendar.getTime.toString + getCSVSeparator + ""
      }
    })
  }
}
