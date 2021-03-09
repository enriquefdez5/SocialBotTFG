package twitterapi

// util libs
import java.util

import twitterapi.TwitterServiceOperations.obtainRtsInfo
import utilities.properties.PropertiesReaderUtil
import utilities.validations.ValidationsUtil

// logging
import org.apache.logging.log4j.scala.Logging

// scala libs
import scala.annotation.tailrec
import scala.collection.JavaConversions._

// app model
import model.Post

// twitter4j libs
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{Paging, Status, StatusUpdate, Twitter, TwitterFactory}

// app imports
import twitterapi.TwitterServiceOperations.{getLastTweetNotReplied, getLastTweetNotRetweeted, statusesToPosts}
import utilities.ConfigRun
import utilities.dates.DatesUtil


object TwitterService extends Logging with PropertiesReaderUtil with ValidationsUtil with DatesUtil {

  val csvSeparator = ","

  /**
   * Function that uses Twitter API to recover the last five tweets from the given user.
   * @param conf, ConfigRun. Param needed to connect to the Twitter API.
   * @param userName, String. It is the username for the user tweets will be collected.
   * @return Seq[Post]. Seq of Post containing the last five tweets the user posted.
   */
  def getLastFiveTweets(conf: ConfigRun, userName: String): Seq[Post] = {
    checkNotEmptyString(userName)
    val pageInit = 1
    val pageSize = 5
    // Obtain twitter client
    val twitter = getTwitterClient(conf)
    val page = new Paging(pageInit, pageSize)
    val tweets: Seq[Status] = twitter.getUserTimeline(userName, page).toSeq
    statusesToPosts(tweets)
  }

  /**
   * Function that uses twitter api to recover around 3200 tweets from the given username.
   * @param conf, ConfigRun. Param needed to use the twitter api.
   * @param userName, String. It is the username for the user tweets will be collected.
   * @return Seq[Post]. Seq of Post containing around the 3200 tweets from the user transformed into application
   * Post objects.
   */
  def getTweets(conf: ConfigRun, userName: String): Seq[Post] = {
    checkNotEmptyString(userName)
    val pageInit = 1
    // Obtain twitter client
    val twitter = getTwitterClient(conf)
    // Get ~3200 user tweets
    val tweets = gatherTweets(twitter, pageInit, userName, Seq())
    statusesToPosts(tweets)
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
   * Function used for creating a twitter client instance
   * @return Twitter, a twitter client instance
   */
  def getTwitterClient(conf: ConfigRun): Twitter = {
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey(conf.consumerTokenKey())
      .setOAuthConsumerSecret(conf.consumerTokenKeySecret())
      .setOAuthAccessToken(conf.accessTokenKey())
      .setOAuthAccessTokenSecret(conf.accessTokenKeySecret())
    new TwitterFactory(cb.build).getInstance()
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

  def getAllActionsOrderedByDate(tweets: Seq[Post], csvTweets: util.ArrayList[String]): Seq[String] = {
    checkNotEmptySeq(tweets)
    checkNotEmptySeq(csvTweets)

    // Pattern for csv file dates
    val csvPattern = "yyyy-MM-dd HH:mm:ss"
    // Patter for twitter dates
    val apiPattern = "EEE MMM dd HH:mm:ss z yyyy"
    // Build date formats
    val simpleDateFormatCSV = getSimpleDateFormat(csvPattern)
    val simpleDateFormatAPI = getSimpleDateFormat(apiPattern)
    // Get calendar instance
    val calendar = getCalendarInstance

    // Get time from tweets read in csv file
    val csvDates = csvTweets.map(tweet => {
      val split = tweet.split(csvSeparator)
      calendar.setTime(simpleDateFormatCSV.parse(split(0)))
      calendar.getTime.toString + csvSeparator + split(2)
    })

    // Filter rts from tweets collected from twitter api
    val rts = obtainRtsInfo(tweets)

    // Create new dates seq item with all csv dates
    val dates = csvDates.map( csvDate => {
      if (csvDate.split(csvSeparator).length > 1) {
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

    // Order by time
    val calendarToOrder = getCalendarInstance
    val orderedDates = dates.sortBy(tweet => {
      val stringToDate = tweet.split(csvSeparator)(0)
      calendarToOrder.setTime(simpleDateFormatAPI.parse(stringToDate))
      calendarToOrder.getTime
    })
//    orderedDates.map(_ + "\n")
    orderedDates
  }

  /**
   * Function that returns the twitter username from the properties file.
   * @return Twitter username from properties file
   */
  def getTwitterUsername: String = {
    getProperties.getProperty("twitterUsername")
  }

}







