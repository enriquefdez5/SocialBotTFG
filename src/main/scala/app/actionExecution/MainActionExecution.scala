package app.actionExecution

import java.util

import model.TypeAndDate
import model.TypeAndDate.postToTypeAndDate
import org.apache.logging.log4j.scala.Logging
import twitterapi.TwitterService.{getLastFiveTweets, getTweets}
import twitterapi.TwitterServiceOperations.{obtainMaxActionsPerHour, obtainMeanActionsPerHour, obtainPostActionsProportion}
import utilities.ConfigRun
import utilities.dates.DatesUtilTraitTrait
import utilities.fileManagement.FileReaderUtilTraitTrait
import utilities.neuralNetworks.NeuralNetworkUtilTraitTraitTrait
import utilities.properties.PropertiesReaderUtilTrait

import scala.annotation.tailrec

object MainActionExecution extends Logging with PropertiesReaderUtilTrait with FileReaderUtilTraitTrait with NeuralNetworkUtilTraitTraitTrait with
  DatesUtilTraitTrait {


  def main(args: Array[String]): Unit = {
    // Twitter API search
    val conf = new ConfigRun(args)

    // Loop for generating and executing actions
    val idx = 0
    val loopLimit = 10
    // Get twitter api tweets
    val tweets = getTweets(conf, getProperties.getProperty("twitterUsername"))

    // Get csv tweets and remove header
    val csvTweets: util.ArrayList[String] = readCSVFile(getProperties.getProperty("csvTweetsFileName"))
    csvTweets.remove(0)

    // Mean and max actions per hour
    val meanActionsPerHour: Int = obtainMeanActionsPerHour(tweets, csvTweets)
    // TODO delete if it will not be used.
    val maxActionsPerHour: Int = obtainMaxActionsPerHour(tweets, csvTweets)

    // Post
    val maxFollowedPostActions: Int = obtainPostActionsProportion(tweets, csvTweets)

    // Get last type and date action from twitter api
    val lastTypeAndDateAction: TypeAndDate = postToTypeAndDate(tweets.head)
    loop(conf, idx, loopLimit, lastTypeAndDateAction, sameHourCount = 1, maxActionsPerHour = meanActionsPerHour,
      followedPostActionsCount = 0, maxFollowedPostActions)
  }


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
      val lastFiveTweetsForNextAction = getLastFiveTweets(conf, getProperties.getProperty("twitterUsername"))
      lastFiveTweetsForNextAction.foreach(tweet => {
      })
      val newTypeAndDateAction: TypeAndDate = generateNextAction(followedPostActionsCount, maxFollowedPostActions,
        lastFiveTweetsForNextAction)
      val isPostAction: Boolean = newTypeAndDateAction.action.value == 1
      val isAtSameHour = newTypeAndDateAction.hourOfDay == lastTypeAndDate.hourOfDay

      logger.debug("-----------------------------")
      logger.debug(newTypeAndDateAction.action.toString)
      logger.debug(newTypeAndDateAction.dayOfWeek.toString)
      logger.debug(newTypeAndDateAction.hourOfDay.toString)
      logger.debug("-----------------------------")
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
   * @param typeAndDate, TypeAndDate item.
   * @param conf, ConfigRun. Item needed to interact with Twitter API.
   */
  private def executeAction(typeAndDate: TypeAndDate, conf: ConfigRun): Unit = {
    val date = buildDate(typeAndDate.dayOfWeek, typeAndDate.hourOfDay)
    val now = getCalendarInstance.getTime
    if (waitForDate(date, now)) {
      typeAndDate.action.execute(conf)
    }
  }
}
