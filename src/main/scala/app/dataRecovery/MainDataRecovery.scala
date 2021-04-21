package app.dataRecovery

// logging
import app.twitterAPI.ConfigRun
import app.twitterAPI.TwitterService.getTwintTweets
import app.twitterAPI.TwitterServiceOperations.getTweetText
import org.apache.logging.log4j.scala.Logging
import utilities.console.ConsoleUtilitiesTrait

// twitterAPI package
import app.twitterAPI.TwitterService.{getActionsWithMonthSeparator, getAllActionsOrderedByDate, getTrainableActions, getTweets}
import app.twitterAPI.TwitterFilterTrait

// utilities import
import utilities.fileManagement.FileWriterUtilTrait
import utilities.properties.PropertiesReaderUtilTrait
import utilities.fileManagement.FileReaderUtilTrait

object MainDataRecovery extends Logging with FileWriterUtilTrait with FileReaderUtilTrait with
  PropertiesReaderUtilTrait with TwitterFilterTrait with ConsoleUtilitiesTrait {


  // Main method for reading tweets and saving in file for later training.
  def main(args: Array[String]): Unit = {
    val startTime = System.currentTimeMillis()

    logger.info("AIBehaviour twitter says hi!")
    // Twitter API search
    val conf = new ConfigRun(args)

    // User input
    val twitterUsernameMsg: String = "Type in twitter username to get data from"
    val twitterUsername: String = askForTwitterUsername(conf, twitterUsernameMsg)

    // language must be spanish or english
    val language: Boolean = askForLanguage()


    // Twint csv path
    val twintCSVTweetsPath: String = "./data(manual)/"

    // Setting character training dataset file location (WRITE to READ)
    val generatedTxtPath: String = "./data(generated)/" + twitterUsername + ".txt"

    // Setting csv actions and dates file location (WRITE to READ)
    val generateCSVPath: String = "./data(generated)/" + twitterUsername + ".csv"

    createDirectories()

    // Get twint tweets
    getTwintTweets(twitterUsername, twintCSVTweetsPath)

    // Read file, remove header and get tweet text
    val tweets = readCSVFile(twintCSVTweetsPath + twitterUsername + ".csv")
    tweets.remove(0)
    val textColumn: Int = 10
    val tweetText = getTweetText(tweets, textColumn)


    // Getting twitter api tweets
    val apiTweets = getTweets(conf, twitterUsername)
    val actionTrainingTweets = getAllActionsOrderedByDate(apiTweets, tweets)

    val actionsWithMonthSeparator = getActionsWithMonthSeparator(actionTrainingTweets)
    val trainableActions = getTrainableActions(actionsWithMonthSeparator)


    // Clean and filter csv tweets for character nn training
    val filteredTweets = cleanTweets(tweetText, language)

    // Add marks to text for the neural network
    val characterTrainingTweets: Seq[String] = markTweets(filteredTweets)

    // Write tweets text in file
    writeDataOnAFile(characterTrainingTweets, generatedTxtPath)

    // Write tweets actions in file
    writeDataOnAFile(trainableActions, generateCSVPath)
    logger.info("AIBheaviour twitter says good bye!")


    val endTime = System.currentTimeMillis()
    val timeElapsed = endTime - startTime
    logger.info("Execution time in seconds: " + timeElapsed/1000.0)
  }
}
