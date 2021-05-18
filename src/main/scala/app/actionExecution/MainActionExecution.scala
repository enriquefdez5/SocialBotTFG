package app.actionExecution

import java.util
import scala.annotation.tailrec
import org.apache.logging.log4j.scala.Logging

import model.NNActionItem
import model.NNActionItem.stringToNNActionItem

import neuralNetworks.NeuralNetworkTrainingTrait

import app.twitterAPI.TwitterService.{ getLastTweet, executeAction }
import app.twitterAPI.TwitterServiceOperations.{ obtainMeanActionsPerHour, getMaxFollowedPostActions }
import app.twitterAPI.ConfigRun

import utilities.dates.DatesUtilTrait
import utilities.fileManagement.FileReaderUtilTrait
import utilities.console.ConsoleUtilTrait

/** Object with main method for action execution
 *
 * It extends Logging, ConsoleUtilTrait, FileReaderUtilTrait, NeuralNetworkTrainingTrait and DatesUtilTrait
 * functionality.
 */
object MainActionExecution extends Logging with ConsoleUtilTrait with
FileReaderUtilTrait with NeuralNetworkTrainingTrait with DatesUtilTrait {

  /** Main class for action execution
   * @param args. Item needed to interact with Twitter API.
   */
  def main(args: Array[String]): Unit = {
    val conf = new ConfigRun(args)

    val twitterUsernameMsg: String = "Type in twitter username to imitate"
    val twitterUsername: String = askForTwitterUsername(conf, twitterUsernameMsg)

    val idx = 0
    val loopLimit = 20

    val typeAndDateTweets = readCSVFile("./data(generated)/" + twitterUsername + ".csv")

    if (typeAndDateTweets.size() == 0) {
      logger.warn("This user has 0 tweets. System will shut dowm.")
      System.exit(1)
    }


    val csvTweets: util.ArrayList[String] = readCSVFile("./data(generated)/" + twitterUsername + ".csv")
    if (csvTweets.size() == 0) {
      logger.warn("This user has 0 tweets. System will shut dowm.")
      System.exit(1)
    }
    val meanActionsPerHour = obtainMeanActionsPerHour(csvTweets)

    val maxFollowedPostActions: Int = getMaxFollowedPostActions(csvTweets)

    val lastTypeAndDateAction: NNActionItem = stringToNNActionItem(csvTweets.get(csvTweets.size() - 1))

    val sameHourCount: Int = 0
    val followedPostActionsCount: Int = 0

    val actionExecutionData: ActionExecutionData = ActionExecutionData(twitterUsername, sameHourCount,
      meanActionsPerHour, followedPostActionsCount, maxFollowedPostActions)
    loop(conf, idx, loopLimit, lastTypeAndDateAction, actionExecutionData)
  }


  /** Main method loop for action execution.
   *
   * @param conf. Item needed to interact with Twitter API.
   * @param idx. Number of iteration.
   * @param lastTypeAndDate. Last action type and date.
   * @param actionExecutionData. Object that contains params needed to execute actions.
   */
  @tailrec
  private def loop(conf: ConfigRun,
                   idx: Int, loopLimit: Int, lastTypeAndDate: NNActionItem,
                   actionExecutionData: ActionExecutionData): Unit = {
    if (idx < loopLimit) {
      val lastTweet = getLastTweet(conf, idx,
                                   actionExecutionData.twitterUsername)

      val newTypeAndDateAction: NNActionItem = generateNextAction(actionExecutionData.twitterUsername,
                                                                  actionExecutionData.followedPostActionsCount,
                                                                  actionExecutionData.maxFollowedPostActions,
                                                                  lastTweet)
      val isPostAction: Boolean = newTypeAndDateAction.commandTrait.get.value == 1
      val isAtSameHour = newTypeAndDateAction.hour.get == lastTypeAndDate.hour.get

      if (isAtSameHour) {
        if (actionExecutionData.sameHourCount < actionExecutionData.meanActionsPerHour) {
          executeAction(actionExecutionData.twitterUsername, newTypeAndDateAction, conf)
          if (isPostAction) {
            loop(conf, idx + 1, loopLimit, lastTypeAndDate,
              ActionExecutionData(actionExecutionData.twitterUsername,
                actionExecutionData.sameHourCount + 1, actionExecutionData.meanActionsPerHour, actionExecutionData
                  .followedPostActionsCount + 1, actionExecutionData.maxFollowedPostActions))
          }
          else {
            loop(conf, idx + 1, loopLimit, lastTypeAndDate,
              ActionExecutionData(actionExecutionData.twitterUsername,
                actionExecutionData.sameHourCount + 1, actionExecutionData.meanActionsPerHour, 0, actionExecutionData
                  .maxFollowedPostActions))
          }
        }
        else {
          logger.info("No more actions can be done at the generated hour")
          logger.info("\nWaiting for the next hour...")
          waitForNextHour()
          loop(conf, idx, loopLimit, newTypeAndDateAction,
            ActionExecutionData(actionExecutionData.twitterUsername,
            actionExecutionData.sameHourCount, actionExecutionData.meanActionsPerHour, actionExecutionData
              .followedPostActionsCount, actionExecutionData.maxFollowedPostActions))
        }
      }
      else {
        executeAction(actionExecutionData.twitterUsername, newTypeAndDateAction, conf)
        if (isPostAction) {
          loop(conf, idx + 1, loopLimit, lastTypeAndDate,
            ActionExecutionData(actionExecutionData.twitterUsername,
              1, actionExecutionData.meanActionsPerHour,
              actionExecutionData.followedPostActionsCount + 1, actionExecutionData.maxFollowedPostActions))
        }
        else {
          loop(conf, idx + 1, loopLimit, lastTypeAndDate,
            ActionExecutionData(actionExecutionData.twitterUsername,
              1, actionExecutionData.meanActionsPerHour, 0, actionExecutionData.maxFollowedPostActions))
        }
      }
    }
  }
}
