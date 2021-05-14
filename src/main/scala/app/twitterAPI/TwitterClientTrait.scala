package app.twitterAPI

import twitter4j.{Twitter, TwitterFactory}
import twitter4j.conf.ConfigurationBuilder

/** Trait for getting a Twitter client instance. */
trait TwitterClientTrait {

  /**
   * @return Twitter client instance
   */
  def getTwitterClient(conf: ConfigRun): Twitter = {
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey(conf.consumerTokenKey())
      .setOAuthConsumerSecret(conf.consumerTokenKeySecret())
      .setOAuthAccessToken(conf.accessTokenKey())
      .setOAuthAccessTokenSecret(conf.accessTokenKeySecret())
    new TwitterFactory(cb.build).getInstance()
  }
}
