package app.dataRecovery

import app.twitterAPI.ConfigRun
import app.twitterAPI.TwitterService.{getTweets, getTwintTweets}
import app.twitterAPI.TwitterServiceOperations.{getActionsWithWeekSeparator, getAllActionsOrderedByDate, getTrainableActions, getTweetText}
import org.apache.logging.log4j.scala.Logging
import utilities.console.ConsoleUtilTrait

import app.twitterAPI.TwitterFilterTrait

import utilities.fileManagement.FileWriterUtilTrait
import utilities.fileManagement.FileReaderUtilTrait


/** Object with main method for data recovery.
 *
 * It extends Logging, FileWriterUtilTrait, FileReaderUtilTrait, TwitterFilterTrait and ConsoleUtilTrait functionality.
 */
object MainDataRecovery extends Logging with FileWriterUtilTrait with FileReaderUtilTrait
                                        with TwitterFilterTrait with ConsoleUtilTrait {


  /** Main method for data recovery
   * @param args. Item needed to interact with Twitter API.
   */
  def main(args: Array[String]): Unit = {
    val selectedOption: Int = mainDataRecoveryExecutionMainMenu(args)
    val conf = new ConfigRun(args)

    val date = askForDate(selectedOption)

    val twitterUsernameMsg: String = "Type in twitter username to get data from"
    val twitterUsername: String = askForTwitterUsername(conf, twitterUsernameMsg)
    val language: Boolean = askForLanguage()


    val twintCSVTweetsPath: String = "./data(manual)/"
    val generatedTxtPath: String = "./data(generated)/" + twitterUsername + ".txt"
    val generateCSVPath: String = "./data(generated)/" + twitterUsername + ".csv"

    createDirectories()

    try {
      getTwintTweets(twitterUsername, twintCSVTweetsPath, selectedOption, date)
      logger.info("Finishing recovering tweets with Twint.")
    }
    catch {
      case exception: Exception => logger.error("There was an exception recovering Twint tweets: " + exception
        .getMessage)
    }
    val tweets = readCSVFile(twintCSVTweetsPath + twitterUsername + ".csv")
    if (tweets.size() > 0) {
      tweets.remove(0)
      val textColumn: Int = 10
      val splitSymbol: String = "\t"

      val tweetText = getTweetText(tweets, textColumn, splitSymbol)

      try {
        val apiTweets = getTweets(conf, twitterUsername, selectedOption, date)
        logger.info("Finishing recovering tweets with Twitter API.")
        val actionTrainingTweets = getAllActionsOrderedByDate(apiTweets, tweets)
        val actionsWithWeekSeparator = getActionsWithWeekSeparator(actionTrainingTweets)
        val trainableActions = getTrainableActions(actionsWithWeekSeparator)

        val filteredTweets = cleanTweets(tweetText, language)
        val characterTrainingTweets: Seq[String] = addLineBreak(filteredTweets)

        writeDataOnAFile(characterTrainingTweets, generatedTxtPath)
        writeDataOnAFile(trainableActions, generateCSVPath)
      }
      catch {
        case exception: Exception => logger.error("There was an exception recovering Twitter API tweets: " +
          exception.getMessage)
      }
    }
    else {
      logger.warn("No tweets were found")
    }
  }
}
