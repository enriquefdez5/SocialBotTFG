package app.actionExecution

import java.io.FileInputStream
import java.util
import java.util.Properties

import model.Action.{POST, REPLY, RT}
import model.TypeAndDate
import model.TypeAndDate.postToTypeAndDate
import org.apache.logging.log4j.scala.Logging
import twitterapi.TwitterService.{getTweets, getTwitterUsername, obtainTweetToReply, obtainTweetToRt}
import twitterapi.TwitterServiceOperations._
import utilities.ConfigRun
import utilities.dates.datesUtil.{buildDate, getCalendarInstance, waitForDate}
import utilities.neuralNetworks.NeuralNetworkUtils.{generateNextAction, prepareText}

import scala.annotation.tailrec

object MainActionExecution extends Logging {


  def main(args: Array[String]): Unit = {
    // Twitter API search
    val conf = new ConfigRun(args)
    // Read properties file
    val properties: Properties = new Properties()
    properties.load(new FileInputStream("src/main/resources/config.properties"))

    // Loop for generating and executing actions
    val idx = 0
    val loopLimit = 10
    val twitterUser = "ibaiLLanos"
    // Get twitter api tweets
    val tweets = getTweets(conf, twitterUser)

    // Get csv tweets and remove header
    val csvTweets: util.ArrayList[String] = utilities.fileManagement.FileReaderUtil
      .readCSVFile("./src/data(not modify)/ibaiLLanos.csv")
    csvTweets.remove(0)

    // Mean and max actions per hour
    val meanActionsPerHour: Int = obtainMeanActionsPerHour(tweets, csvTweets)
    // TODO delete if it will not be used.
    val maxActionsPerHour: Int = obtainMaxActionsPerHour(tweets, csvTweets)

    // Post
    val maxFollowedPostActions: Int = obtainMaxFollowedPostActions(tweets, csvTweets)

    // Get last type and date action from twitter api
    val lastTypeAndDateAction: TypeAndDate = postToTypeAndDate(tweets.head)
    loop(conf, idx, loopLimit, lastTypeAndDateAction, sameHourCount = 1, maxActionsPerHour = meanActionsPerHour,
      followedPostActionsCount = 0, maxFollowedPostActions)
  }


  // Third loop method with checks for actions in hour and followed post actions
  /**
   * This method contains the loop for generating and executing actions.
   *
   * @param conf      . Item needed to interact with Twitter API.
   * @param idx       . Value that represents the iteration number.
   * @param loopLimit . Limit value. It defines the loop stop condition.
   */
  @tailrec
  private def loop(conf: ConfigRun, idx: Int, loopLimit: Int, lastTypeAndDate: TypeAndDate,
                   sameHourCount: Int, maxActionsPerHour: Int,
                   followedPostActionsCount: Int, maxFollowedPostActions: Int): Unit = {
    if (idx < loopLimit) {
      val newTypeAndDateAction: TypeAndDate = generateNextAction(followedPostActionsCount, maxFollowedPostActions, conf)
      val isPostAction: Boolean = newTypeAndDateAction.action == POST
      val isAtSameHour = newTypeAndDateAction.hourOfDay == lastTypeAndDate.hourOfDay
      // Action at same hour
      if (isAtSameHour) {
        // Another action at same hour can be done
        if (sameHourCount < maxActionsPerHour) {
          executeAction(newTypeAndDateAction, conf)
          if (isPostAction) {
            loop(conf, idx + 1, loopLimit, lastTypeAndDate,
              sameHourCount + 1, maxActionsPerHour,
              followedPostActionsCount + 1, maxFollowedPostActions)
          }
          else {
            loop(conf, idx + 1, loopLimit, lastTypeAndDate,
              sameHourCount + 1, maxActionsPerHour,
              0, maxFollowedPostActions)
          }
        }
        else {
          logger.debug("No more actions can be done at the generated hour")
          loop(conf, idx, loopLimit, newTypeAndDateAction,
            sameHourCount, maxActionsPerHour,
            followedPostActionsCount, maxFollowedPostActions)
        }
      }
      // Action at different hour
      else {
        executeAction(newTypeAndDateAction, conf)
        if (isPostAction) {
          loop(conf, idx + 1, loopLimit, lastTypeAndDate,
            sameHourCount = 1, maxActionsPerHour,
            followedPostActionsCount + 1, maxFollowedPostActions)
        }
        else {
          loop(conf, idx + 1, loopLimit, lastTypeAndDate,
            sameHourCount = 1, maxActionsPerHour,
            0, maxFollowedPostActions)
        }
      }
    }
  }

  /**
   * Function that will execute an action following typeAndDate object
   *
   * @param typeAndDate item.
   * @param conf        . Item needed to interact with Twitter API.
   */
  private def executeAction(typeAndDate: TypeAndDate, conf: ConfigRun): Unit = {
    val date = buildDate(typeAndDate.dayOfWeek, typeAndDate.hourOfDay)
    val now = getCalendarInstance.getTime
    if (waitForDate(date, now)) {
      typeAndDate.action match {
        case POST => executePost(conf)
        case RT => executeReply(conf)
        case REPLY => executeRt(conf)
      }
    }
  }

  /**
   * Function that prepares a post action that will be executed.
   *
   * @param conf . Item needed to interact with Twitter API.
   */
  private def executePost(conf: ConfigRun): Unit = {
    // Text to post
    val nCharactersToSample: Int = 200
    val tweetText: String = prepareText(nCharactersToSample)
    // Post
    logger.debug("Tweet posted")
    //    postTweet(tweetText, conf)
  }

  /**
   * Function that prepares a reply action that will be executed.
   *
   * @param conf . Item needed to interact with Twitter API.
   */
  private def executeReply(conf: ConfigRun): Unit = {
    // Prepare text
    val nCharactersToSample: Int = 120
    val replyText: String = prepareText(nCharactersToSample)
    // Get twitter username
    val twitterUsername = getTwitterUsername
    // Get tweets
    val tweets = twitterapi.TwitterService.getTweets(conf, twitterUsername)
    // Get most replied user from gathered tweets
    val mostRepliedUserId: Long = obtainMostRepliedUserId(tweets)
    // Get tweet to reply
    val tweetToReplyId: Long = obtainTweetToReply(mostRepliedUserId, conf)
    // Reply
    logger.debug("Reply tweet")
    //    replyTweet(replyText, mostRepliedUserId, tweetToReplyId, conf)
  }

  /**
   * Function that prepares a rt action that will be executed.
   *
   * @param conf . Item needed to interact with Twitter API.
   */
  private def executeRt(conf: ConfigRun): Unit = {
    // Get twitter username
    val twitterUserName = getTwitterUsername
    // Get tweets
    val tweets = getTweets(conf, twitterUserName)
    // Get most retweeted User from gathered tweets
    val mostRetweetedUserId: Long = obtainMostRetweetedUserId(tweets)
    // Get tweet to rt
    val tweetToRt: Long = obtainTweetToRt(mostRetweetedUserId, conf)
    // Rt
    logger.debug("Retweet tweet")
    //    rtTweet(tweetToRt, conf)
  }
}
