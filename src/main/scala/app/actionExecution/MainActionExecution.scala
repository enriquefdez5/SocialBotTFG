package app.actionExecution

// java, scala and logging package
import java.util
import org.apache.logging.log4j.scala.Logging
import scala.annotation.tailrec


// App package
import app.twitterAPI.ConfigRun

// Utilities package
import utilities.console.ConsoleUtilitiesTrait

// model package
import model.NNActionItem
import model.NNActionItem.postToTypeAndDate

// neural networks package
import neuralNetworks.NeuralNetworkTrainingTrait

// twitterAPI package
import app.twitterAPI.TwitterService.{getLastTweet, getTweets}
import app.twitterAPI.TwitterServiceOperations.{obtainMaxActionsPerHour, getMeanActionsPerHour, getMaxFollowedPostActions}

// utilities package
import utilities.dates.DatesUtilTrait
import utilities.fileManagement.FileReaderUtilTrait
import utilities.properties.PropertiesReaderUtilTrait

object MainActionExecution extends Logging with ConsoleUtilitiesTrait with PropertiesReaderUtilTrait with
FileReaderUtilTrait with NeuralNetworkTrainingTrait with DatesUtilTrait {


  def main(args: Array[String]): Unit = {
    // Twitter API search
    val conf = new ConfigRun(args)

    val twitterUsernameMsg: String = "Type in twitter username to imitate"
    val twitterUsername: String = askForTwitterUsername(conf, twitterUsernameMsg)


    val twitterWhereToPostMsg: String = "Type in twitter username where actions will be executed"
    val twitterWhereToPost: String = askForTwitterUsername(conf, twitterWhereToPostMsg)
    // Loop for generating and executing actions
    val idx = 0
    val loopLimit = 20
    // Get twitter api tweets
    val tweets = getTweets(conf, twitterUsername)

    // Get csv tweets and remove header
    val csvTweets: util.ArrayList[String] = readCSVFile("./data(manual)/" + twitterUsername + ".csv")
    csvTweets.remove(0)

    // Mean and max actions per hour
    val meanActionsPerHour: Int = getMeanActionsPerHour(tweets, csvTweets)

    logger.debug("meanActionsPerHour: " + meanActionsPerHour)

    // Post
    val maxFollowedPostActions: Int = getMaxFollowedPostActions(tweets, csvTweets)

    // Get last type and date action from twitter api
    val lastTypeAndDateAction: NNActionItem = postToTypeAndDate(tweets.head)

    val sameHourCount: Int = 0
    val followedPostActionsCount: Int = 0

    val actionExecutionData: ActionExecutionData = ActionExecutionData(twitterUsername, twitterWhereToPost, sameHourCount,
      meanActionsPerHour, followedPostActionsCount, maxFollowedPostActions)
    loop(conf, idx, loopLimit, lastTypeAndDateAction, actionExecutionData)
  }


  /**
   * This method contains the loop for generating and executing actions.
   *
   * @param conf      . Item needed to interact with Twitter API.
   * @param idx       . Value that represents the iteration number.
   * @param loopLimit . Limit value. It defines the loop stop condition.
   */
  @tailrec
  private def loop(conf: ConfigRun,
                   idx: Int, loopLimit: Int, lastTypeAndDate: NNActionItem,
                   actionExecutionData: ActionExecutionData): Unit = {
    if (idx < loopLimit) {
      val lastTweet = getLastTweet(conf, idx,
                                   actionExecutionData.twitterUsername,
                                   actionExecutionData.twitterUsernameWhereToPost)

      // se actualiza con la app.
      val newTypeAndDateAction: NNActionItem = generateNextAction(actionExecutionData.twitterUsername,
                                                                  actionExecutionData.followedPostActionsCount,
                                                                  actionExecutionData.maxFollowedPostActions,
                                                                  lastTweet)
      val isPostAction: Boolean = newTypeAndDateAction.action.value == 1
      val isAtSameHour = newTypeAndDateAction.hourOfDay == lastTypeAndDate.hourOfDay

      logger.debug("-----------------------------")
      logger.debug("Generated action type: " + newTypeAndDateAction.action.toString)
      logger.debug("Generated action day of week: " + newTypeAndDateAction.dayOfWeek.toString)
      logger.debug("Generated action hour of day: " + newTypeAndDateAction.hourOfDay.toString)
      logger.debug("-----------------------------")
      // Action at same hour
      if (isAtSameHour) {
        // Another action at same hour can be done
        if (actionExecutionData.sameHourCount < actionExecutionData.meanActionsPerHour) {
          executeAction(actionExecutionData.twitterUsername, newTypeAndDateAction, conf)
          if (isPostAction) {
            loop(conf, idx + 1, loopLimit, lastTypeAndDate,
              ActionExecutionData(actionExecutionData.twitterUsername, actionExecutionData.twitterUsernameWhereToPost,
                actionExecutionData.sameHourCount + 1, actionExecutionData.meanActionsPerHour, actionExecutionData
                  .followedPostActionsCount + 1, actionExecutionData.maxFollowedPostActions))
          }
          else {
            loop(conf, idx + 1, loopLimit, lastTypeAndDate,
              ActionExecutionData(actionExecutionData.twitterUsername, actionExecutionData.twitterUsernameWhereToPost,
                actionExecutionData.sameHourCount + 1, actionExecutionData.meanActionsPerHour, 0, actionExecutionData
                  .maxFollowedPostActions))
          }
        }
        else {
          logger.debug("No more actions can be done at the generated hour")
          waitForNextHour()
          loop(conf, idx, loopLimit, newTypeAndDateAction,
            ActionExecutionData(actionExecutionData.twitterUsername, actionExecutionData.twitterUsernameWhereToPost,
            actionExecutionData.sameHourCount, actionExecutionData.meanActionsPerHour, actionExecutionData
              .followedPostActionsCount, actionExecutionData.maxFollowedPostActions))
        }
      }
      // Action at different hour
      else {
        executeAction(actionExecutionData.twitterUsername, newTypeAndDateAction, conf)
        if (isPostAction) {
          loop(conf, idx + 1, loopLimit, lastTypeAndDate,
            ActionExecutionData(actionExecutionData.twitterUsername, actionExecutionData.twitterUsernameWhereToPost,
              1, actionExecutionData.meanActionsPerHour,
              actionExecutionData.followedPostActionsCount + 1, actionExecutionData.maxFollowedPostActions))
        }
        else {
          loop(conf, idx + 1, loopLimit, lastTypeAndDate,
            ActionExecutionData(actionExecutionData.twitterUsername, actionExecutionData.twitterUsernameWhereToPost,
              1, actionExecutionData.meanActionsPerHour, 0, actionExecutionData.maxFollowedPostActions))
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
  private def executeAction(twitterUsername: String, typeAndDate: NNActionItem, conf: ConfigRun): Unit = {
    val date = buildDate(typeAndDate.dayOfWeek, typeAndDate.hourOfDay)
    val now = getCalendarInstance.getTime
    if (waitForDate(date, now)) {
      typeAndDate.action.execute(twitterUsername, conf)
      waitForNextAction()
    }
  }
}
