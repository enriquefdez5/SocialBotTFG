package model.commandActions

import app.twitterAPI.ConfigRun
import app.twitterAPI.TwitterService.{getTweets, obtainTweetToRt, rtTweet}
import app.twitterAPI.TwitterServiceOperations.obtainMostRetweetedUserId

/**
 * Class that represents a Retweet command action
 */
class RtCommand extends ActionCommandTrait {

  val value = 3

  /**
   * Execute a post action.
   * @param twitterUsername. Twitter username.
   * @param conf. Item built with main args to interact with Twitter API.
   */
  override def execute(twitterUsername: String, conf: ConfigRun): Unit = {
    // Get tweets
    val tweets = getTweets(conf, twitterUsername)
    // Get most retweeted User from gathered tweets
    val mostRetweetedUserId: Long = obtainMostRetweetedUserId(tweets)
    // Get tweet to rt
    val tweetToRt: Long = obtainTweetToRt(mostRetweetedUserId, conf)
    // Rt
    rtTweet(tweetToRt, conf)
  }

}
