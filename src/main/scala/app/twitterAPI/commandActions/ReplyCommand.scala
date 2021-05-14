package model.commandActions

import app.twitterAPI.ConfigRun
import app.twitterAPI.TwitterService.{getTweets, obtainTweetToReply, replyTweet}
import app.twitterAPI.TwitterServiceOperations.obtainMostRepliedUserId
import neuralNetworks.NeuralNetworkTrainingTrait


/**
 * Class that represents a Reply command action
 */
class ReplyCommand extends ActionCommandTrait with NeuralNetworkTrainingTrait {

  val value = 2

  /**
   * Execute a post action.
   * @param twitterUsername. Twitter username.
   * @param conf. Item built with main args to interact with Twitter API.
   */
  override def execute(twitterUsername: String, conf: ConfigRun): Unit = {
    // Prepare text
    val nCharactersToSample: Int = 120
    val replyText: String = prepareText(twitterUsername, nCharactersToSample)
    // Get tweets
    val tweets = getTweets(conf, twitterUsername)
    // Get most replied user from gathered tweets
    val mostRepliedUserId: Long = obtainMostRepliedUserId(conf, tweets)
    // Get tweet to reply
    val tweetToReplyId: Long = obtainTweetToReply(mostRepliedUserId, conf)
    // Reply
    replyTweet(replyText, mostRepliedUserId, tweetToReplyId, conf)
  }

}
