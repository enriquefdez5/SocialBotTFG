package app.twitterAPI

// util libs
import java.text.SimpleDateFormat
import java.util

import model.StatusImpl
import org.json.JSONArray
import app.twitterAPI.TwitterServiceOperations.{getLastTweetNotReplied, getLastTweetNotRetweeted, statusesToStatusImpl}
import app.twitterAPI.TwitterServiceOperations.obtainRtsInfo
import utilities.properties.PropertiesReaderUtilTrait
import utilities.validations.ValidationsUtilTrait

import scala.collection.mutable

// logging
import org.apache.logging.log4j.scala.Logging

// scala libs
import scala.annotation.tailrec
import scala.collection.JavaConversions._

import scala.sys.process._



// twitter4j libs
import twitter4j.{Paging, Status, StatusUpdate, Twitter}

// app imports
import utilities.dates.DatesUtilTrait


object TwitterService extends Logging with TwitterClientTrait with PropertiesReaderUtilTrait with ValidationsUtilTrait
  with DatesUtilTrait {


  def getCSVSeparator: String = {
    ","
  }
  def getTwintSeparator: String = {
    "\t"
  }


  def getTwintTweets(username: String, csvFileNamePath: String): Unit = {
    val command = "twint -u " + username + " -o " + csvFileNamePath + username + ".csv --csv"
    command.!!
  }

  /**
   * Function that uses Twitter API to recover the last five tweets from the given user.
   * @param conf, ConfigRun. Param needed to connect to the Twitter API.
   * @param userName, String. It is the username for the user tweets will be collected.
   * @return Seq[StatusImpl]. Seq of StatusImpl containing the last five tweets the user posted.
   */
  def getLastTweet(conf: ConfigRun, idx: Int,
                   twitterUsername: String, twitterUsernameWhereToPost: String): Seq[StatusImpl] = {
    checkNotEmptyString(twitterUsername)
    checkNotEmptyString(twitterUsernameWhereToPost)


    val pageInit = 1
    val pageSize = 1
    // Obtain twitter client
    val twitter = getTwitterClient(conf)
    val page = new Paging(pageInit, pageSize)
    if (idx == 0) {
      val tweets: Seq[Status] = twitter.getUserTimeline(twitterUsername, page).toSeq
      statusesToStatusImpl(tweets)
    }
    else {
      val tweets: Seq[Status] = twitter.getUserTimeline(twitterUsernameWhereToPost, page).toSeq
      statusesToStatusImpl(tweets)
    }
  }

  /**
   * Function that uses twitter api to recover around 3200 tweets from the given username.
   * @param conf, ConfigRun. Param needed to use the twitter api.
   * @param userName, String. It is the username for the user tweets will be collected.
   * @return Seq[StatusImpl]. Seq of StatusImpl containing around the 3200 tweets from the user transformed into application
   * StatusImpl objects.
   */
  def getTweets(conf: ConfigRun, userName: String): Seq[StatusImpl] = {
    checkNotEmptyString(userName)
    val pageInit = 1
    // Obtain twitter client
    val twitter = getTwitterClient(conf)
    // Get ~3200 user tweets
    val tweets = gatherTweets(twitter, pageInit, userName, Seq())
    statusesToStatusImpl(tweets)
  }


  /**
   * Function for collecting tweets
   * @param twitter, client to request operation
   * @param pageInit, number of page from which tweets are collected
   * @param userName, user profile where to search
   * @param tweets, list of tweets collected
   */
  @tailrec
  private def gatherTweets(twitter: Twitter, pageInit: Int, userName: String, tweets: Seq[Status]): Seq[Status] = {
    checkNotNegativeInt(pageInit)
    checkNotEmptyString(userName)

    if (tweets.size < getProperties.getProperty("maxNumberTweetsAllowed").toInt) {
      val page = new Paging(pageInit, getProperties.getProperty("gatheringTweetsPageSize").toInt)
      val newTweets: Seq[Status] = twitter.getUserTimeline(userName, page).toSeq
      logger.debug(s"Gathered ${newTweets.size()} tweets")
      gatherTweets(twitter, pageInit + 1, userName, tweets ++ newTweets)
    }
    else {
      checkNotEmptySeq(tweets)
      tweets
    }
  }

  /**
   * Function that post a tweet on twitter.
   * @param tweet, String. It is the content of the tweet that is going to be posted.
   * @param conf, ConfigRun. Param needed to connect to the Twitter API.
   */
  def postTweet(tweet: String, conf: ConfigRun): Unit = {
    checkNotEmptyString(tweet)

    val twitter = getTwitterClient(conf)
    twitter.updateStatus(tweet)
  }

  /**
   * Function that retweets a tweet on twitter.
   * @param tweetId, Long. It is the identifier of the tweet that is going to be retweeted.
   * @param conf, ConfigRun. Param needed to connect to the Twitter API.
   */
  def rtTweet(tweetId: Long, conf: ConfigRun): Unit = {

    checkNotNegativeLong(tweetId)

    val twitter = getTwitterClient(conf)
    twitter.retweetStatus(tweetId)
  }

  /**
   * Function that replies a tweet on twitter.
   * @param tweetText, String. It is the content of the tweet that is going to be posted to reply another tweet.
   * @param mostRepliedUserId, Long. It is the idenfier of the user that will be replied. It is needed to form the
   * username nick in order to reply a tweet.
   * @param replyTweetId, Long. It is the identifier of the tweet to be replied.
   * @param conf, ConfigRun. Param needed to connect to the Twitter API.
   */
  def replyTweet(tweetText: String, mostRepliedUserId: Long, replyTweetId: Long, conf: ConfigRun ): Unit = {
    checkNotEmptyString(tweetText)
    checkNotNegativeLong(mostRepliedUserId)
    checkNotNegativeLong(replyTweetId)

    val twitter = getTwitterClient(conf)
    // Build API parameter with "@username. " to reply tweet
    val username = "@" + twitter.showUser(mostRepliedUserId).getName + ". "
    val fullTweet = username + tweetText
    // Build API parameter
    val statusUpdate: StatusUpdate = new StatusUpdate(fullTweet).inReplyToStatusId(replyTweetId)
    // Reply
    twitter.updateStatus(statusUpdate)
  }

  /**
   * Function that obtains the last tweet not retweeted given a user id.
   * @param mostRetweetedUserId, Long. The user id of the most retweeted user.
   * @param conf, ConfigRun. Param needed to connect to the Twitter API.
   * @return Long. The tweet id to be retweeted.
   */
  def obtainTweetToRt(mostRetweetedUserId: Long, conf: ConfigRun): Long = {
    checkNotNegativeLong(mostRetweetedUserId)

    val twitter = getTwitterClient(conf)
    val idx = 0
    getLastTweetNotRetweeted(twitter.getUserTimeline(mostRetweetedUserId), idx).getId
  }

  /**
   * Function that obtains the last tweet not replied given a user id.
   * @param mostRepliedUserId, Long. The user id of the most replied user.
   * @param conf, ConfigRun. Param needed to connect to the Twitter API.
   * @return Long. The tweet id to be replied.
   */
  def obtainTweetToReply(mostRepliedUserId: Long, conf: ConfigRun): Long = {
    checkNotNegativeLong(mostRepliedUserId)

    val twitter = getTwitterClient(conf)
    val idx = 0
    getLastTweetNotReplied(twitter.getUserTimeline(mostRepliedUserId), idx).getId
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
        case exception: Exception => {
          ""
        }
      }
    }
    else {
      ""
    }

  }

  def getTrainableActions(actions: Seq[String]): Seq[String] = {
    val calendar = getCalendarInstance
    val pattern = "EEE MMM dd HH:mm:ss z yyyy"
    val parser = new SimpleDateFormat(pattern)
    actions.map(action => {
      if (action != "-1\n") {
        val splitAction = action.split(getCSVSeparator)
        calendar.setTime(parser.parse(splitAction(0)))
        getCalendarDay(calendar) + getCSVSeparator + getCalendarHour(calendar) + getCSVSeparator + splitAction(2)
      }
      else { action }
    })
  }

  def getActionsWithMonthSeparator(actionTrainingTweets: Seq[String]): Seq[String] = {
    var lastDay = 0
    val calendar = getCalendarInstance
    val pattern = "EEE MMM dd HH:mm:ss z yyyy"
    val parser = new SimpleDateFormat(pattern)

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

  def getAllActionsOrderedByDate(tweets: Seq[StatusImpl], csvTweets: util.ArrayList[String])
  : Seq[String] = {
    checkNotEmptySeq(tweets)
    checkNotEmptySeq(csvTweets)

    // Get unique tweets
    val distinctCSVTweets = csvTweets.distinct
    // Get date from those tweets
    val csvDates = getCSVDates(distinctCSVTweets)

    // Filter rts from tweets collected from twitter api
    val rts = obtainRtsInfo(tweets)

    // Create new dates seq item with all csv dates
    val dates = csvDates.map( csvDate => {
      if (csvDate.split(getCSVSeparator).length > 1) {
        csvDate + ",2"
      }
      else {
        csvDate + ",1"
      }
    })

    // Add rt dates into dates seq item.
    rts.foreach(rt => {
      dates.add(rt)
    })

    orderDates(dates)
  }

  private def orderDates(dates: mutable.Buffer[String]): mutable.Buffer[String] = {
    // Patter for twitter dates
    val apiPattern = "EEE MMM dd HH:mm:ss z yyyy"
    // Order by time
    val simpleDateFormatAPI = getSimpleDateFormat(apiPattern)

    val calendarToOrder = getCalendarInstance
    val orderedDates = dates.sortBy(tweet => {
      val stringToDate = tweet.split(getCSVSeparator)(0)
      calendarToOrder.setTime(simpleDateFormatAPI.parse(stringToDate))
      calendarToOrder.getTime
    })

//    orderedDates.reverse.map(_+"\n")
    orderedDates.map(_ + "\n")
  }


  private def getCSVDates(distinctCSVTweets: mutable.Buffer[String]): mutable.Buffer[String] = {
    // Pattern for csv file dates
    val csvPattern = "yyyy-MM-dd HH:mm:ss"

    // Build date formats
    val simpleDateFormatCSV = getSimpleDateFormat(csvPattern)


    // Get time from tweets read in csv file
    val dateColumn = 3
    val timeColumn = 4
    val mentionColumn = 31

    // Get calendar instance
    val calendar = getCalendarInstance

    distinctCSVTweets.map(tweet => {
      try {
        val split = tweet.split(getTwintSeparator)
        calendar.setTime(simpleDateFormatCSV.parse(split(dateColumn) + " " + split(timeColumn)))
        calendar.getTime.toString + getCSVSeparator + getMentionName(split(mentionColumn))
      }
      catch {
        case exception: Exception => {
          calendar.getTime.toString + getCSVSeparator + ""
        }
      }
    })
  }

}







