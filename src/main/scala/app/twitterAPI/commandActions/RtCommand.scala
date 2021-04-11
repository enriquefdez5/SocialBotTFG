package app.twitterAPI.commandActions

import app.twitterAPI.ConfigRun
import org.apache.logging.log4j.scala.Logging
import app.twitterAPI.TwitterService.{getTweets, obtainTweetToRt, rtTweet}
import app.twitterAPI.TwitterServiceOperations.obtainMostRetweetedUserId
import utilities.properties.PropertiesReaderUtilTrait

class RtCommand extends Logging with ActionCommandTrait with PropertiesReaderUtilTrait {

  val value = 3

  override def execute(conf: ConfigRun): Unit = {
    // Get tweets
    val tweets = getTweets(conf, getProperties.getProperty("twitterUsername"))
    // Get most retweeted User from gathered tweets
    val mostRetweetedUserId: Long = obtainMostRetweetedUserId(tweets)
    // Get tweet to rt
    val tweetToRt: Long = obtainTweetToRt(mostRetweetedUserId, conf)
    // Rt
    logger.debug("Retweet tweet")
    rtTweet(tweetToRt, conf)
  }

}
