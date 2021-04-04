package twitterapi.commandActions

// logging import
import org.apache.logging.log4j.scala.Logging

// twitter api conf item import
import utilities.ConfigRun

// import nn prepare text function
import utilities.neuralNetworks.NeuralNetworkUtilTraitTraitTrait

class PostCommandTrait extends Logging with ActionCommandTrait with NeuralNetworkUtilTraitTraitTrait {

  val value = 1
  
  override def execute(conf: ConfigRun): Unit = {
    // Text to post
    val nCharactersToSample: Int = 200
    val tweetText: String = prepareText(nCharactersToSample)
    // Post
    logger.debug("Tweet posted" + tweetText)
//        postTweet(tweetText, conf)
  }

}
