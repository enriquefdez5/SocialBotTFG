package app.dataRecovery

// Windows cmd
import twitterapi.TwitterService.{getAllActionsOrderedByDate, getTweets}
import utilities.properties.PropertiesReaderUtil

// Uncomment if cmd is needed
 import scala.sys.process._

// cmd input for twitterusername
import scala.io.StdIn.readLine

// properties file

// java conversions
import scala.collection.JavaConversions._

// Uncomment if got could be used
// import twitterapi.getOldTweets.Got.{getOldTweetsByUsername, getTextFromGetOldTweets}

import utilities.fileManagement.FileReaderUtil

// logging
import org.apache.logging.log4j.scala.Logging

// twitter api imports
import twitterapi.TwitterFilter.{cleanTweets, markTweets}

// utilities import
import utilities.ConfigRun
import utilities.fileManagement.FileWriterUtil

object MainDataRecovery extends Logging with FileWriterUtil with FileReaderUtil with PropertiesReaderUtil {


  // Main method for reading tweets and saving in file for later training.
  def main(args: Array[String]): Unit = {
    logger.info("AIBehaviour twitter says hi!")
    // Twitter API search
    val conf = new ConfigRun(args)

    // User input
    val twitterUsernameProperty = "twitterUsername"
    val twitterUsername: String = readLine("Type in twitter username to get tweets from \n")

    // language must be spanish or english
    val language: Boolean = readLine("Type in user language S for spanish, E for english \n") == "S"


    // Setting properties based on input
    // Setting twitter username
    getProperties.setProperty(twitterUsernameProperty, twitterUsername)

    // Setting GOT3 csv file location (READ)
    val csvTweetsFileNamePropertyName: String = "csvTweetsFileName"
    val csvTweetsFileNamePath: String = "./data(manual)/"
    getProperties.setProperty(csvTweetsFileNamePropertyName, csvTweetsFileNamePath + getProperties.getProperty
    (twitterUsernameProperty) + ".csv")

    // Setting character training dataset file location (WRITE to READ)
    val dataSetFileNamePropertyName: String = "dataSetFileName"
    val generatePath: String = "./data(generated)/"
    getProperties.setProperty(dataSetFileNamePropertyName, generatePath + getProperties.getProperty
    (twitterUsernameProperty) + ".txt")

    // Setting csv actions and dates file location (WRITE to READ)
    val csvActionsPropertyName: String = "actionsCSVFileName"
    val generateActionsPath: String = "./data(generated)/"
    getProperties.setProperty(csvActionsPropertyName, generateActionsPath + getProperties.getProperty
    (twitterUsernameProperty) + ".csv")

    // Update properties file with new info
    saveProperties()

    // Executing cmd command. Using sys.process to execute a command and get the file into app.
    val command = "twint -u " + twitterUsername + " -o " + csvTweetsFileNamePath + getProperties.getProperty(twitterUsernameProperty) + ".csv --csv"
    command.!!

    // Read file, remove header and get tweet text
    val textColumn: Int = 10
    val tweets = readCSVFile()
    tweets.remove(0)
    val tweetText = tweets.map(_.split("\t")(textColumn).replace("\"", ""))

    // Getting twitter api tweets
    val apiTweets = getTweets(conf, getProperties.getProperty("twitterUsername"))
    val actionTrainingTweets = getAllActionsOrderedByDate(apiTweets, tweets)


    // Clean and filter csv tweets for character nn training
    val filteredTweets = cleanTweets(tweetText, language)

    // Add marks to text for the neural network
    val characterTrainingTweets: Seq[String] = markTweets(filteredTweets)

    // Write tweets text in file
    writeDataOnAFile(characterTrainingTweets)

    // Write tweets actions in file
    writeDataOnAFile(actionTrainingTweets, getProperties.getProperty(csvActionsPropertyName))
    logger.info("AIBheaviour twitter says good bye!")
  }
}
