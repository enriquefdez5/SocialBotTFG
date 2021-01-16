import utilities.{ConfigRun, IOUtil}
//import twitterapi.TwitterService.{getTweets, transformCSVFile}
import twitterapi.TwitterService.getTweets
import org.apache.logging.log4j.scala.Logging

object Main extends Logging{

  def main(args: Array[String]): Unit = {
    val conf = new ConfigRun(args)

    logger.info("AIBehaviour twitter says hi!")

    // Twitter username where tweets will be search
    val twitterUser = "sanchezcastejon"

    // Get tweets from twitter.
    val tweets = getTweets(conf, twitterUser)

    // Obtain tweets from csv file and rtweets from twitter account
//    val anotherCSVFile = transformCSVFile(tweets)

    // Write train data in a file
    IOUtil.writeDataOnAFile(anotherCSVFile, fileName = "./trainCSV.csv")

    logger.info("AIBheaviour twitter says good bye!")
  }
}
