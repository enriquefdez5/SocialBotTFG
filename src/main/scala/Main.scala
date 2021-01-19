// Java conversions
import utilities.fileManagement.FileWriterUtil

import scala.collection.JavaConversions._

// twitterapi functinos
import twitterapi.TwitterFilter.{cleanTweets, markTweets}
import twitterapi.TwitterService.getTweets

// logging service
import org.apache.logging.log4j.scala.Logging

// utilities
import utilities.ConfigRun
import utilities.fileManagement.{FileReaderUtil, FileWriterUtil}

object Main extends Logging {

  // Main method for reading tweets and saving in file for later training.
  def main(args: Array[String]): Unit = {
    logger.info("AIBehaviour twitter says hi!")
    // Twitter API search
    val conf = new ConfigRun(args)
    // Twitter username where tweets will be search
    val twitterUser = "ibaiLLanos"
    // Get tweets from twitter
    val apiTweets = getTweets(conf, twitterUser)

//    val tweetText = tweets.map(_.text)

    // Get tweets from library CSV file
    val csvFileName = "ibaiLLanos.csv"
    val textColumn: Int = 6
    val tweets = FileReaderUtil.readCSVFile(csvFileName)
    val tweetText = tweets.toSeq.map(_.split(",")(textColumn).replace("\"", ""))

    // Clean and filter those tweets
    val filteredTweets = cleanTweets(tweetText)
    // Add marks to text for the neural network
    val markedTweets: Seq[String] = markTweets(filteredTweets)

    // Write text in file
    FileWriterUtil.writeDataOnAFile(markedTweets)

    // Write train data in a file
    IOUtil.writeDataOnAFile(anotherCSVFile, fileName = "./trainCSV.csv")

    logger.info("AIBheaviour twitter says good bye!")
  }
}
