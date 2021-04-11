package app.actionExecution

// java, scala and logging package
import java.util

import app.twitterAPI.ConfigRun
import neuralNetworks.NeuralNetworkTrainingTrait
import org.apache.logging.log4j.scala.Logging
import utilities.console.ConsoleUtilitiesTrait

import scala.annotation.tailrec

// model package
import model.NNActionItem
import model.NNActionItem.postToTypeAndDate

// twitterAPI package
import app.twitterAPI.TwitterService.{getLastFiveTweets, getTweets}
import app.twitterAPI.TwitterServiceOperations.{obtainMaxActionsPerHour, obtainMeanActionsPerHour, obtainPostActionsProportion}

// utilities package
import utilities.dates.DatesUtilTrait
import utilities.fileManagement.FileReaderUtilTrait
import utilities.properties.PropertiesReaderUtilTrait

object MainActionExecution extends Logging with ConsoleUtilitiesTrait with PropertiesReaderUtilTrait with
FileReaderUtilTrait with NeuralNetworkTrainingTrait with DatesUtilTrait {


  def main(args: Array[String]): Unit = {
    // Twitter API search
    val conf = new ConfigRun(args)

    val twitterUsername: String = askForTwitterUsername(conf)

    // Loop for generating and executing actions
    val idx = 0
    val loopLimit = 20
    // Get twitter api tweets
    val tweets = getTweets(conf, twitterUsername)

    // Get csv tweets and remove header
    val csvTweets: util.ArrayList[String] = readCSVFile("/data(manual)/" + twitterUsername)
    csvTweets.remove(0)

    // Mean and max actions per hour
    val meanActionsPerHour: Int = obtainMeanActionsPerHour(tweets, csvTweets)
    // TODO delete if it will not be used.
    val maxActionsPerHour: Int = obtainMaxActionsPerHour(tweets, csvTweets)

    logger.debug("meanActionsPerHour: " + meanActionsPerHour)
    logger.debug("maxActionsPerHour: " + maxActionsPerHour)
    // Post
    val maxFollowedPostActions: Int = obtainPostActionsProportion(tweets, csvTweets)

    // Get last type and date action from twitter api
    val lastTypeAndDateAction: NNActionItem = postToTypeAndDate(tweets.head)
    loop(conf, twitterUsername, idx, loopLimit, lastTypeAndDateAction, sameHourCount = 1, maxActionsPerHour =
      meanActionsPerHour, followedPostActionsCount = 0, maxFollowedPostActions)
  }


  /**
   * This method contains the loop for generating and executing actions.
   *
   * @param conf      . Item needed to interact with Twitter API.
   * @param idx       . Value that represents the iteration number.
   * @param loopLimit . Limit value. It defines the loop stop condition.
   */
  @tailrec
  private def loop(conf: ConfigRun, twitterUsername: String, idx: Int, loopLimit: Int, lastTypeAndDate: NNActionItem,
                   sameHourCount: Int, maxActionsPerHour: Int,
                   followedPostActionsCount: Int, maxFollowedPostActions: Int): Unit = {
    if (idx < loopLimit) {
      val lastFiveTweetsForNextAction = getLastFiveTweets(conf, getProperties.getProperty("twitterUsername"))
      lastFiveTweetsForNextAction.foreach(it => {
      })
      val newTypeAndDateAction: NNActionItem = generateNextAction(twitterUsername, followedPostActionsCount,
        maxFollowedPostActions, lastFiveTweetsForNextAction)
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
            loop(conf, twitterUsername, idx + 1, loopLimit, lastTypeAndDate,
              sameHourCount + 1, maxActionsPerHour,
              followedPostActionsCount + 1, maxFollowedPostActions)
          }
          else {
            loop(conf, twitterUsername, idx + 1, loopLimit, lastTypeAndDate,
              sameHourCount + 1, maxActionsPerHour,
              0, maxFollowedPostActions)
          }
        }
        else {
          logger.debug("No more actions can be done at the generated hour")
          loop(conf, twitterUsername, idx, loopLimit, newTypeAndDateAction,
            sameHourCount, maxActionsPerHour,
            followedPostActionsCount, maxFollowedPostActions)
        }
      }
      // Action at different hour
      else {
        executeAction(newTypeAndDateAction, conf)
        if (isPostAction) {
          loop(conf, twitterUsername, idx + 1, loopLimit, lastTypeAndDate,
            sameHourCount = 1, maxActionsPerHour,
            followedPostActionsCount + 1, maxFollowedPostActions)
        }
        else {
          loop(conf, twitterUsername, idx + 1, loopLimit, lastTypeAndDate,
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
  private def executeAction(typeAndDate: NNActionItem, conf: ConfigRun): Unit = {
    val date = buildDate(typeAndDate.dayOfWeek, typeAndDate.hourOfDay)
    val now = getCalendarInstance.getTime
    if (waitForDate(date, now)) {
      typeAndDate.action.execute(conf)
      waitForNextAction()
    }
  }
}
