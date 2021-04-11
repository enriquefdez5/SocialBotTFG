package app.twitterAPI.commandActions

// logging import
import app.twitterAPI.ConfigRun
import app.twitterAPI.TwitterService.postTweet
import neuralNetworks.NeuralNetworkTrainingTrait
import org.apache.logging.log4j.scala.Logging

// twitter api conf item import

// import nn prepare text function

class PostCommand extends Logging with ActionCommandTrait with NeuralNetworkTrainingTrait {

  val value = 1
  
  override def execute(conf: ConfigRun): Unit = {
    // Text to post
    val nCharactersToSample: Int = 200
    val tweetText: String = prepareText(nCharactersToSample)
    // Post
    logger.debug("Tweet posted: " + tweetText)
//    postTweet(tweetText, conf)
  }

}
