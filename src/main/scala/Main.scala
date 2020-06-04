import utilities.ConfigRun
import twitterapi.TwitterService.getTweets
import twitterapi.TwitterFilter.{cleanTweets, markTweets}
import facebookapi.FacebookService.getNewsFeed
import org.apache.logging.log4j.scala.Logging
import utilities.FileManagement.{FileReaderUtil, FileWriterUtil}


object Main extends Logging {

  def main(args: Array[String]): Unit = {
    val conf = new ConfigRun(args)
    logger.info("AIBehaviour twitter says hi!")
    logger.info("Searching for twitter posts")

    // Twitter username where tweets will be search
    val twitterUser = "sanchezcastejon"

    // Get tweets from twitter
    val tweets = getTweets(conf, twitterUser)

    // Clean those tweets
    val filteredTweets = cleanTweets(tweets)

    // Add marks to text for the neural network
    val markedTweets = markTweets(filteredTweets)

    // Search for FB post.
//    logger.info("Searching for facebook posts")
//
//    val fbPosts = getNewsFeed()
//
//    logger.debug("-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-")
//    logger.debug(fbPosts.toString)

    // Clean FB text

    // It could be done with Instagram too
    // Search for FB post.

    // Clean FB text

    // Write text in file
    FileWriterUtil.writeDataOnAFile(markedTweets)

    // Append FB text to the training file.

    // This could be included in another main method called from here
    // Get data from file
    val data = FileReaderUtil.readDataFromAFile()

    logger.info("AIBheaviour twitter says good bye!")
  }
}
