package app.dataRecovery

// Windows cmd
import twitterapi.TwitterService.{getAllActionsOrderedByDate, getTweets}
import utilities.properties.PropertiesReaderUtil.saveProperties

import scala.sys.process._

// cmd input for twitterusername
import scala.io.StdIn.readLine

// properties file
import utilities.properties.PropertiesReaderUtil.getProperties

// java conversions
import scala.collection.JavaConversions._

//import twitterapi.getOldTweets.Got.{getOldTweetsByUsername, getTextFromGetOldTweets}
import utilities.fileManagement.FileReaderUtil

// logging
import org.apache.logging.log4j.scala.Logging

// twitter api imports
import twitterapi.TwitterFilter.{cleanTweets, markTweets}

// utilities import
import utilities.ConfigRun
import utilities.fileManagement.FileWriterUtil

object MainDataRecovery extends Logging {


  // Main method for reading tweets and saving in file for later training.
  def main(args: Array[String]): Unit = {
    logger.info("AIBehaviour twitter says hi!")
    // Twitter API search
    val conf = new ConfigRun(args)

    // User input
    val twitterUsernameProperty = "twitterUsername"
    val twitterUsername: String = readLine("Type in twitter username to get tweets from")

    // language must be spanish or english
    val language: Boolean = readLine("Type in user language S for spanish, E for english") == "S"


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
    saveProperties

    // Getting csv tweets
    // TODO see how to code it
        // Coding. Using library getOldTweets4j locally imported
//    val tweets = getOldTweetsByUsername(getProperties.getProperty("twitterUsername"))
//    val tweetText = getTextFromGetOldTweets(tweets)

        // Reading from manual csv. Csv obtained by executing cmd command outside app and importing file into app
        // manually
    val textColumn: Int = 6
    val tweets = FileReaderUtil.readCSVFile()
    tweets.remove(0)
    val tweetText = tweets.map(_.split(",")(textColumn).replace("\"", ""))

        // Executing cmd command. Using sys.process to execute a command and get the file into app.
//    val command = "python ./Optimized-Modified-GetOldTweets3-OMGOT-master/GetOldTweets3.py --username " +
//      "\"angelmartin_nc\" --maxtweets 10000"
//    val output = command.!!
//    logger.debug(command)


    // Getting twitter api tweets
    val apiTweets = getTweets(conf, getProperties.getProperty("twitterUsername"))
    val actionTrainingTweets = getAllActionsOrderedByDate(apiTweets, tweets)



    // Clean and filter csv tweets for character nn training
    val filteredTweets = cleanTweets(tweetText, language)
    // Add marks to text for the neural network
    val characterTrainingTweets: Seq[String] = markTweets(filteredTweets)



    // Write tweets text in file
    FileWriterUtil.writeDataOnAFile(characterTrainingTweets)

    // Write tweets actions in file
    FileWriterUtil.writeDataOnAFile(actionTrainingTweets, getProperties.getProperty(csvActionsPropertyName))
    logger.info("AIBheaviour twitter says good bye!")
  }
}
