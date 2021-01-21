package twitterapi.commandActions

import app.actionExecution.MainActionExecution.logger
import org.apache.logging.log4j.scala.Logging
import twitterapi.TwitterService.{getTwitterUsername, obtainTweetToReply}
import twitterapi.TwitterServiceOperations.obtainMostRepliedUserId
import utilities.ConfigRun
import utilities.neuralNetworks.NeuralNetworkUtils.prepareText

class ReplyCommand extends Logging with ActionCommand {


  val value = 3

  override def getValue(): Int = {
    value
  }

  override def execute(conf: ConfigRun): Unit = {
    // Prepare text
    val nCharactersToSample: Int = 120
    val replyText: String = prepareText(nCharactersToSample)
    // Get twitter username
    val twitterUsername = getTwitterUsername
    // Get tweets
    val tweets = twitterapi.TwitterService.getTweets(conf, twitterUsername)
    // Get most replied user from gathered tweets
    val mostRepliedUserId: Long = obtainMostRepliedUserId(tweets)
    // Get tweet to reply
    val tweetToReplyId: Long = obtainTweetToReply(mostRepliedUserId, conf)
    // Reply
    logger.debug("Reply tweet")
    //    replyTweet(replyText, mostRepliedUserId, tweetToReplyId, conf)
  }

}
