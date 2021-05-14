package app.twitterAPI.commandActions

import app.twitterAPI.ConfigRun
import app.twitterAPI.TwitterService.{getTweets, obtainTweetToRt, rtTweet}
import app.twitterAPI.TwitterServiceOperations.obtainMostRetweetedUserId

import org.apache.logging.log4j.scala.Logging

/** Class that represents a Retweet command action */
class RtCommand extends Logging with ActionCommandTrait {

  val value = 3

  /** Execute a post action.
   *
   * @param twitterUsername. Twitter username.
   * @param conf. Item built with main args to interact with Twitter API.
   */
  override def execute(twitterUsername: String, conf: ConfigRun): Unit = {
    val tweets = getTweets(conf, twitterUsername)
    val mostRetweetedUserId: Long = obtainMostRetweetedUserId(tweets)
    val tweetToRt: Long = obtainTweetToRt(mostRetweetedUserId, conf)

    logger.info("Tweet with id '" + tweetToRt + "' has been retweeted.")
    rtTweet(tweetToRt, conf)
  }

}
