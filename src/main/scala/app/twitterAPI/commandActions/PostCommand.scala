package app.twitterAPI.commandActions

import app.twitterAPI.ConfigRun
import app.twitterAPI.TwitterService.postTweet

import neuralNetworks.NeuralNetworkTrainingTrait

import org.apache.logging.log4j.scala.Logging

/** Class that represents a Post command action */
class PostCommand extends Logging with ActionCommandTrait with NeuralNetworkTrainingTrait {

  val value = 1

  /** Execute a post action.
   *
   * @param twitterUsername. Twitter username.
   * @param conf. Item built with main args to interact with Twitter API.
   */
  override def execute(twitterUsername: String, conf: ConfigRun): Unit = {
    val nCharactersToSample: Int = 200
    val tweetText: String = prepareText(twitterUsername, nCharactersToSample)

    logger.info("Tweet has been posted with content '" + tweetText + "'.")
    postTweet(tweetText, conf)
  }
}
