package twitterapi.commandActions

import org.apache.logging.log4j.scala.Logging
import twitterapi.TwitterService.obtainTweetToReply
import twitterapi.TwitterServiceOperations.obtainMostRepliedUserId
import utilities.ConfigRun
import utilities.neuralNetworks.NeuralNetworkUtils.prepareText
import utilities.properties.PropertiesReaderUtil.getProperties

class ReplyCommand extends Logging with ActionCommand {

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
