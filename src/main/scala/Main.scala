import java.io.FileInputStream
import java.util.Properties
import scala.collection.JavaConversions._

import twitterapi.TwitterFilter.{cleanTweets, markTweets}
import org.apache.logging.log4j.scala.Logging
import utilities.FileManagement.{FileReaderUtil, FileWriterUtil}


object Main extends Logging {

  // Main method for reading tweets and saving in file for later training.
  def main(args: Array[String]): Unit = {

    // Read properties file
    val properties: Properties = new Properties()
    properties.load(new FileInputStream("src/main/resources/config.properties"))

    logger.info("AIBehaviour twitter says hi!")
    logger.info("Searching for twitter posts")

    // Twitter API search
//    val conf = new ConfigRun(args)
    // Twitter username where tweets will be search
//    val twitterUser = "sanchezcastejon"
    // Get tweets from twitter
//    val tweets = getTweets(conf, twitterUser)
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

    logger.info("AIBheaviour twitter says good bye!")
  }
}
