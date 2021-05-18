package app.twitterAPI

import app.twitterAPI.TwitterServiceOperations.{getLastTweetNotReplied, getLastTweetNotRetweeted, statusesToStatusImpl}
import model.{NNActionItem, StatusImpl}

import java.util.{Calendar, Date}
import org.apache.logging.log4j.scala.Logging
import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.sys.process._

import twitter4j.TwitterException
import twitter4j.{Paging, Status, StatusUpdate, Twitter}

import utilities.dates.DatesUtilTrait
import utilities.fileManagement.FileWriterUtilTrait
import utilities.properties.PropertiesReaderUtilTrait
import utilities.validations.ValidationsUtilTrait


/** Object that contains the functions needed to access to Twitter API and get or post data. */
object TwitterService extends Logging with TwitterClientTrait with ValidationsUtilTrait with PropertiesReaderUtilTrait
                                      with DatesUtilTrait with FileWriterUtilTrait {

  /** Execute an action.
   *
   * @param nNActionItem Object with type and date information
   * @param conf, ConfigRun. Object built to interact with Twitter API.
   */
  def executeAction(twitterUsername: String, nNActionItem: NNActionItem, conf: ConfigRun): Unit = {
    val date = buildDate(nNActionItem.day.get, nNActionItem.hour.get)
    logger.info("Next action will be executed at '" + date.toString + "'.")
    val now = getCalendarInstance.getTime
    if (waitForDate(date, now)) {
      nNActionItem.commandTrait.get.execute(twitterUsername, conf)
      waitForNextAction()
    }
  }

  /** Post a tweet.
   *
   * @param tweet Tweet text.
   * @param conf Item built with main args to interact with Twitter API.
   */
  def postTweet(tweet: String, conf: ConfigRun): Unit = {
    checkNotEmptyString(tweet)

    val twitter = getTwitterClient(conf)
    twitter.updateStatus(tweet)
  }

  /** Retweet a tweet.
   *
   * @param tweetId Tweet to retweet identifier.
   * @param conf Item built with main args to interact with Twitter API.
   */
  def rtTweet(tweetId: Long, conf: ConfigRun): Unit = {
    checkNotNegativeLong(tweetId)

    val twitter = getTwitterClient(conf)
    try {
      twitter.retweetStatus(tweetId)
    }
    catch {
      case exception: TwitterException => logger.error("Tweet could not be retweeted. " + exception.getMessage)
    }
  }

  /** Reply a tweet
   *
   * @param tweet Tweet text.
   * @param mostRepliedUserId Most replied user identifier.
   * @param replyTweetId Tweet to reply identifier.
   * @param conf Item built with main args to interact with Twitter API.
   */
  def replyTweet(tweet: String, mostRepliedUserId: Long, replyTweetId: Long, conf: ConfigRun ): Unit = {
    checkNotEmptyString(tweet)
    checkNotNegativeLong(mostRepliedUserId)
    checkNotNegativeLong(replyTweetId)

    val twitter = getTwitterClient(conf)
    val username = "@" + twitter.showUser(mostRepliedUserId).getName + ". "
    val fullTweet = username + tweet
    val statusUpdate: StatusUpdate = new StatusUpdate(fullTweet).inReplyToStatusId(replyTweetId)

    twitter.updateStatus(statusUpdate)
  }



  /** Get twint tweets and write them into a csv file
   *
   * @param twitterUsername Twitter username.
   * @param csvFileNamePath Csv file path.
   * @param selectedOption Selected option to know how to make the query
   * @param date Date since tweets will be recovered
   * @return Twint tweets content
   */
  def getTwintTweets(twitterUsername: String, csvFileNamePath: String, selectedOption: Int, date: Date = new Date())
  : String = {
    selectedOption match {
      case 1 =>
          val command = "twint -u " + twitterUsername + " -o " + csvFileNamePath + twitterUsername + ".csv --csv"
          command.!!
      case 2 =>
        val calendar = getCalendarInstance
        calendar.setTime(date)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        val command = "twint -u " + twitterUsername + " --since \"" + year + "-" + month + "-" + day + " " + hour +
          ":" + minutes + ":" + seconds + "\"" + " -o " + csvFileNamePath + twitterUsername + ".csv --csv"
        command.!!
    }
  }

  /** Get tweets from Twitter API.
   *
   * @param conf Item built with main args to interact with Twitter API.
   * @param twitterUsername Twitter username.
   * @return sequence of tweets objects.
   */
  def getTweets(conf: ConfigRun, twitterUsername: String, selectedOption: Int = 0, date: Date = new Date())
  : Seq[StatusImpl] = {
    checkNotEmptyString(twitterUsername)
    val pageInit = 1
    val twitter = getTwitterClient(conf)
    logger.info("Recovering tweets from Twitter.")
    val tweets = getTweetsFromSelectedOption(twitter, pageInit, twitterUsername, Seq(), selectedOption, date)
    statusesToStatusImpl(tweets)
  }

  private def getTweetsFromSelectedOption(twitter: Twitter, pageInit: Int, twitterUserName: String, seq: Seq[Status],
                                          selectedOption: Int, date: Date): Seq[Status] = {
    if (selectedOption == 2) {
      gatherTweets(twitter, pageInit, twitterUserName, seq, date)
    }
    else {
      gatherTweets(twitter, pageInit, twitterUserName, Seq())
    }
  }

  @tailrec
  private def gatherTweets(twitter: Twitter, pageInit: Int, userName: String, tweets: Seq[Status],
                           sinceDate: Date = getOldestHour): Seq[Status] = {
    checkNotNegativeInt(pageInit)
    checkNotEmptyString(userName)

    if (tweets.size < getProperties.getProperty("maxNumberTweetsAllowed").toInt ) {
      val page = new Paging(pageInit, getProperties.getProperty("gatheringTweetsPageSize").toInt)
      new Paging()
      val newTweets: Seq[Status] = twitter.getUserTimeline(userName, page).toSeq
      if (newTweets.isEmpty) {
        tweets
      }
      else if (sinceDate.after(newTweets.get(0).getCreatedAt)) {
        tweets.filter(tweet => tweet.getCreatedAt.after(sinceDate))
      }
      else if (newTweets.nonEmpty) {
        gatherTweets(twitter, pageInit + 1, userName, tweets ++ newTweets, sinceDate)
      }
      else {
        val filtered = tweets.filter(tweet => tweet.getCreatedAt.after(sinceDate))
        filtered
      }
    }
    else {
      checkNotEmptySeq(tweets)
      tweets.filter(tweet => tweet.getCreatedAt.after(sinceDate))
    }
  }

  /** Get last tweets published.
   *
   * @param conf Item built with main args to interact with the Twitter API.
   * @param twitterUsername Twitter username.
   * @return Seq of StatusImpl containing the last five tweets the user posted.
   */
  def getLastTweet(conf: ConfigRun, idx: Int, twitterUsername: String): Seq[StatusImpl] = {
    checkNotEmptyString(twitterUsername)

    val pageInit = 1
    val pageSize = 1

    val twitter = getTwitterClient(conf)
    val page = new Paging(pageInit, pageSize)
    if (idx == 0) {
      val tweets: Seq[Status] = twitter.getUserTimeline(twitterUsername, page).toSeq
      statusesToStatusImpl(tweets)
    }
    else {
      val tweets: Seq[Status] = twitter.getHomeTimeline(page).toSeq
      statusesToStatusImpl(tweets)
    }
  }

  /** Obtain a tweet to retweet.
   *
   * @param mostRetweetedUserId Most retweeted user identifier.
   * @param conf Item built with main args to interact with Twitter API.
   * @return The tweet to be retweeted identifier.
   */
  def obtainTweetToRt(mostRetweetedUserId: Long, conf: ConfigRun): Long = {
    checkNotNegativeLong(mostRetweetedUserId)

    val twitter = getTwitterClient(conf)
    val idx = 0
    getLastTweetNotRetweeted(twitter.getUserTimeline(mostRetweetedUserId), idx).getId
  }

  /** Obtain a tweet to reply
   *
   * @param mostRepliedUserId Most replied user identifier.
   * @param conf Item built with main args to interact with Twitter API.
   * @return Long. The tweet id to be replied.
   */
  def obtainTweetToReply(mostRepliedUserId: Long, conf: ConfigRun): Long = {
    checkNotNegativeLong(mostRepliedUserId)

    val twitter = getTwitterClient(conf)
    val idx = 0
    getLastTweetNotReplied(twitter.getUserTimeline(mostRepliedUserId), idx).getId
  }
}







