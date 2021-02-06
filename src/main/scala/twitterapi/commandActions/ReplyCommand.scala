package twitterapi.commandActions

import org.apache.logging.log4j.scala.Logging
import twitterapi.TwitterService.obtainTweetToReply
import twitterapi.TwitterServiceOperations.obtainMostRepliedUserId
import utilities.ConfigRun
import utilities.neuralNetworks.NeuralNetworkUtils
import utilities.properties.PropertiesReaderUtil

class ReplyCommand extends Logging with ActionCommand with PropertiesReaderUtil with NeuralNetworkUtils{

  val value = 3

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
    //    replyTweet(replyText, mostRepliedUserId, tweetToReplyId, conf)
  }

}
