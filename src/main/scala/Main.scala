import utilities.{ConfigRun, IOUtil}
import twitterapi.TwitterService.getTweets
import twitterapi.TwitterFilter.{cleanTweets, markTweets}
import org.apache.logging.log4j.scala.Logging

object Main extends Logging{

  def main(args: Array[String]): Unit = {
    val conf = new ConfigRun(args)

    logger.info("AIBehaviour twitter says hi!")
    // Twitter username where tweets will be search
    val twitterUser = "sanchezcastejon"

    // Get tweets from twitter
    val tweets = getTweets(conf, twitterUser)

    // Clean those tweets
    val filteredTweets = cleanTweets(tweets)

    // Add marks to text for the neural network
    val markedTweets = markTweets(filteredTweets)
    // Search for FB post.

    // Clean FB text

    // It could be done with Instagram too
    // Search for FB post.

    // Clean FB text


    // Write text in file
    IOUtil.writeDataOnAFile(markedTweets)

    // Append FB text to the training file.


    // Training model could be another MainMethod
    logger.info("AIBheaviour twitter says good bye!")
  }
}
