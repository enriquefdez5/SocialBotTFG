package twitterapi.commandActions

import app.actionExecution.MainActionExecution.logger
import org.apache.logging.log4j.scala.Logging
import twitterapi.TwitterService.{getTweets, getTwitterUsername, obtainTweetToRt}
import twitterapi.TwitterServiceOperations.obtainMostRetweetedUserId
import utilities.ConfigRun

class RtCommand extends Logging with ActionCommand {

  val value = 2

  override def getValue(): Int = {
    value
  }

  override def execute(conf: ConfigRun): Unit = {
    // Get twitter username
    val twitterUserName = getTwitterUsername
    // Get tweets
    val tweets = getTweets(conf, twitterUserName)
    // Get most retweeted User from gathered tweets
    val mostRetweetedUserId: Long = obtainMostRetweetedUserId(tweets)
    // Get tweet to rt
    val tweetToRt: Long = obtainTweetToRt(mostRetweetedUserId, conf)
    // Rt
    logger.debug("Retweet tweet")
    //    rtTweet(tweetToRt, conf)
  }

}
