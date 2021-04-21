package app.twitterAPI.commandActions

import app.twitterAPI.ConfigRun
import org.apache.logging.log4j.scala.Logging
import app.twitterAPI.TwitterService.{getTweets, obtainTweetToReply, replyTweet}
import app.twitterAPI.TwitterServiceOperations.obtainMostRepliedUserId
import neuralNetworks.NeuralNetworkTrainingTrait
import utilities.properties.PropertiesReaderUtilTrait

class ReplyCommand extends Logging with ActionCommandTrait with PropertiesReaderUtilTrait with
  NeuralNetworkTrainingTrait {

  val value = 2

  override def execute(twitterUsername: String, conf: ConfigRun): Unit = {
    // Prepare text
    val nCharactersToSample: Int = 120
    val replyText: String = prepareText(twitterUsername, nCharactersToSample)
    // Get tweets
    val tweets = getTweets(conf, twitterUsername)
    // Get most replied user from gathered tweets
    val mostRepliedUserId: Long = obtainMostRepliedUserId(tweets)
    // Get tweet to reply
    val tweetToReplyId: Long = obtainTweetToReply(mostRepliedUserId, conf)
    // Reply
    logger.debug("Reply tweet")
    logger.debug("User id:" + mostRepliedUserId)
    logger.debug("Tweet to reply id:" + tweetToReplyId)
    logger.debug("Tweet text:" + replyText)
    replyTweet(replyText, mostRepliedUserId, tweetToReplyId, conf)
  }

}
