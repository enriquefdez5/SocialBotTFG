package twitterapi.getOldTweets

import me.jhenrique.manager.{TweetManager, TwitterCriteria}
import me.jhenrique.model.Tweet

import org.apache.logging.log4j.scala.Logging

import scala.collection.JavaConversions._

object Got extends Logging {


  def getOldTweetsByUsername(username: String): java.util.List[Tweet] = {
    val maxTweetsToGather = 10000
    val criteria: TwitterCriteria = TwitterCriteria.create()
      .setUsername(username)
      .setMaxTweets(maxTweetsToGather)
    TweetManager.getTweets(criteria)
  }

  def getTextFromGetOldTweets(tweets: java.util.List[Tweet]): Seq[String] = {
    tweets.map(_.getText)
  }
}
