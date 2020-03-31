import utilities.{IOUtil, Logger}
import twitterapi.TwitterService.getTweets
import twitterapi.TwitterFilter.cleanTweets
import org.apache.logging.log4j.Level

object Main {

  def main(args: Array[String]): Unit = {
    Logger.log(Level.INFO,"AIBehaviour twitter says hi!" )
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
    Logger.log(Level.INFO,"AIBheaviour twitter says good bye!" )
  }
}
