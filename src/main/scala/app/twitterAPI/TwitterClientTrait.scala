package app.twitterAPI

import twitter4j.{Twitter, TwitterFactory}
import twitter4j.conf.ConfigurationBuilder

trait TwitterClientTrait {

  /**
   * Function used for creating a twitter client instance
   * @return Twitter, a twitter client instance
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
