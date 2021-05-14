package app.twitterAPI.commandActions

import app.twitterAPI.ConfigRun
import app.twitterAPI.TwitterService.{getTweets, obtainTweetToReply, replyTweet}
import app.twitterAPI.TwitterServiceOperations.obtainMostRepliedUserId
import neuralNetworks.NeuralNetworkTrainingTrait


/** Class that represents a Reply command action */
class ReplyCommand extends ActionCommandTrait with NeuralNetworkTrainingTrait {

  val value = 2

  /** Execute a post action.
   *
   * @param twitterUsername. Twitter username.
   * @param conf. Item built with main args to interact with Twitter API.
   */
  override def execute(twitterUsername: String, conf: ConfigRun): Unit = {
    val nCharactersToSample: Int = 120
    val replyText: String = prepareText(twitterUsername, nCharactersToSample)

    val tweets = getTweets(conf, twitterUsername)
    val mostRepliedUserId: Long = obtainMostRepliedUserId(conf, tweets)
    val tweetToReplyId: Long = obtainTweetToReply(mostRepliedUserId, conf)

    logger.info("Tweet with id '" + tweetToReplyId + "' from user with id '" + mostRepliedUserId + "' has been" +
      "replied with content '" + replyText + "'.")
    replyTweet(replyText, mostRepliedUserId, tweetToReplyId, conf)
  }

}
