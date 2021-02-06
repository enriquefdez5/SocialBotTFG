package twitterapi.commandActions

import org.apache.logging.log4j.scala.Logging
import twitterapi.TwitterService.{getTweets, obtainTweetToRt}
import twitterapi.TwitterServiceOperations.obtainMostRetweetedUserId
import utilities.ConfigRun
import utilities.properties.PropertiesReaderUtil

class RtCommand extends Logging with ActionCommand with PropertiesReaderUtil {

  val value = 2

  override def execute(conf: ConfigRun): Unit = {
    // Get tweets
    val tweets = getTweets(conf, getProperties.getProperty("twitterUsername"))
    // Get most retweeted User from gathered tweets
    val mostRetweetedUserId: Long = obtainMostRetweetedUserId(tweets)
    // Get tweet to rt
    val tweetToRt: Long = obtainTweetToRt(mostRetweetedUserId, conf)
    // Rt
    logger.debug("Retweet tweet")
    //    rtTweet(tweetToRt, conf)
  }

}
