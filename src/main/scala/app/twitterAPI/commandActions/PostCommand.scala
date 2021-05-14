package model.commandActions

// logging import
import app.twitterAPI.ConfigRun
import app.twitterAPI.TwitterService.postTweet
import neuralNetworks.NeuralNetworkTrainingTrait
import org.apache.logging.log4j.scala.Logging

/**
 * Class that represents a Post command action
 */
class PostCommand extends Logging with ActionCommandTrait with NeuralNetworkTrainingTrait {

  val value = 1

  /**
   * Execute a post action.
   * @param twitterUsername. Twitter username.
   * @param conf. Item built with main args to interact with Twitter API.
   */
  override def execute(twitterUsername: String, conf: ConfigRun): Unit = {
    // Text to post
    val nCharactersToSample: Int = 200
    val tweetText: String = prepareText(twitterUsername, nCharactersToSample)
    // Post
    logger.debug("Tweet posted: " + tweetText)
    postTweet(tweetText, conf)
  }
}
