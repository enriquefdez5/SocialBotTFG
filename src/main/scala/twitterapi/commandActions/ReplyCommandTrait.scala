package twitterapi.commandActions

import org.apache.logging.log4j.scala.Logging
import twitterapi.TwitterService.obtainTweetToReply
import twitterapi.TwitterServiceOperations.obtainMostRepliedUserId
import utilities.ConfigRun
import utilities.neuralNetworks.NeuralNetworkUtilTraitTraitTrait
import utilities.properties.PropertiesReaderUtilTrait

class ReplyCommandTrait extends Logging with ActionCommandTrait with PropertiesReaderUtilTrait with NeuralNetworkUtilTraitTraitTrait{

  val value = 2

  override def execute(conf: ConfigRun): Unit = {
    // Prepare text
    val nCharactersToSample: Int = 120
    val replyText: String = prepareText(nCharactersToSample)
    // Get tweets
    val tweets = twitterapi.TwitterService.getTweets(conf, getProperties.getProperty("twitterUsername"))
    // Get most replied user from gathered tweets
    val mostRepliedUserId: Long = obtainMostRepliedUserId(tweets)
    // Get tweet to reply
    val tweetToReplyId: Long = obtainTweetToReply(mostRepliedUserId, conf)
    // Reply
    logger.debug("Reply tweet")
    logger.debug("User id:" + mostRepliedUserId)
    logger.debug("Tweet to reply id:" + tweetToReplyId)
    logger.debug("Tweet text:" + replyText)
    //    replyTweet(replyText, mostRepliedUserId, tweetToReplyId, conf)
  }

}
