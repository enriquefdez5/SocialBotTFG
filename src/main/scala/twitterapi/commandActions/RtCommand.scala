package twitterapi.commandActions

import org.apache.logging.log4j.scala.Logging
import twitterapi.TwitterService.{getTweets, obtainTweetToRt}
import twitterapi.TwitterServiceOperations.obtainMostRetweetedUserId
import utilities.properties.PropertiesReaderUtil.getProperties
import utilities.ConfigRun

class RtCommand extends Logging with ActionCommand {

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
