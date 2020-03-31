import utilities.IOUtil
import twitterapi.TwitterService.getTweets
import twitterapi.TwitterFilter.cleanTweets
import org.apache.logging.log4j.scala.Logging

object Main extends Logging {

  def main(args: Array[String]): Unit = {
    logger.info("AIBehaviour twitter says hi!" )
    //Twitter username where tweets will be search
    val twitterUser = "sanchezcastejon"

    //Get tweets from twitter
    val tweets =  getTweets(twitterUser)

    //Clean those tweets
    val filteredTweets = cleanTweets(tweets)

    //Search for FB post.

    //Clean FB text

    //It could be done with Instagram too
    //Search for FB post.

    //Clean FB text


    //Write text in file
    IOUtil.writeDataOnAFile(filteredTweets)

    //Append FB text to the training file.


    //Training model could be another MainMethod
    logger.info("AIBheaviour twitter says good bye!" )
  }
}
