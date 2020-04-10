package twitterapi

// util libs
import java.io.FileInputStream
import java.util.Properties

import utilities.ConfigRun
import org.apache.logging.log4j.scala.Logging

// scala libs
import scala.collection.JavaConversions._
import scala.annotation.tailrec

// app libs
import model.{Plataforma, Post, User, UserInfo, UserStats, ProfileImg}

// twitter4j libs
import twitter4j.{Paging, Status, TwitterFactory, Twitter}
import twitter4j.conf.ConfigurationBuilder

object TwitterService extends Logging {

  // Read properties file
  val properties: Properties = new Properties()
  properties.load(new FileInputStream("src/main/resources/config.properties"))

  def getTweets(conf: ConfigRun, userName: String): Seq[Post] = {
    val pagInit = 1

    // Obtain twitter client
    val twitter = getTwitterClient(conf)

    // Get profile data
    val userSearched = twitter.showUser(userName)
    logger.debug(s"Showing $userName profile Info")

    // Create profile just in case I need it for NN or something
    val user = createUser(userSearched)

    logger.debug(userSearched.toString)

    // Get ~3200 user tweets
    val tweets = gatherTweets(twitter, pagInit, userName, user, Seq())
    tweets.map(tweet => Post(tweet.getId, user, tweet.getText, tweet.getCreatedAt.toString, tweet.getRetweetCount,
      tweet.getFavoriteCount, tweet.getGeoLocation, tweet.isRetweeted, tweet.isFavorited, Plataforma.twitter))
  }

    /**
     * Function for collecting tweets
     * @param twitter, client to request operation
     * @param pageInit, number of page from which tweets are collected
     * @param userName, user profile where to search
     * @param user, app object that represents a user profile
     * @param tweets, list of tweets collected
     */
  @tailrec
  private def gatherTweets(twitter: Twitter, pageInit: Int, userName: String, user: User,
                                 tweets: Seq[Status]): Seq[Status] = {
    if (tweets.size < properties.getProperty("maxNumberTweetsAllowed").toInt) {
      val page = new Paging(pageInit, properties.getProperty("gatheringTweetsPageSize").toInt)
      val newTweets: Seq[Status] = twitter.getUserTimeline(userName, page).toSeq
      logger.debug(s"Gathered ${newTweets.size()} tweets")
      gatherTweets(twitter, pageInit + 1, userName, user, tweets ++ newTweets)
    }
    else { tweets }
  }



  /**
   * Method for creating a twitter client instane
   * @return Twitter, a twitter client instance
   */
  private def getTwitterClient(conf: ConfigRun): Twitter = {
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey(conf.consumerTokenKey())
      .setOAuthConsumerSecret(conf.consumerTokenKeySecret())
      .setOAuthAccessToken(conf.accessTokenKey())
      .setOAuthAccessTokenSecret(conf.accessTokenKeySecret())
    val tf = new TwitterFactory(cb.build)
    val twitter = tf.getInstance
    twitter
  }


  /**
   *  Method for creating an object with user info needed
   * @param userSearched with info needed to create the app User
   * @return User, app user object
   */
  private def createUser(userSearched: twitter4j.User): User = User(id = userSearched.getId, strId = userSearched.getId.toString,
    userInfo = UserInfo(name = userSearched.getName,
      description = userSearched.getDescription, createdAt = userSearched.getCreatedAt.toString,
      location = userSearched.getLocation, profileUrl = userSearched.getURL),
    userStats = UserStats(followers = userSearched.getFollowersCount, followings = userSearched.getFriendsCount,
      isProtected = userSearched.isProtected, isVerified = userSearched.isVerified),
    profileImg = ProfileImg(defaultProfileImgUrl = userSearched.isDefaultProfileImage,
      profileImgUrl = userSearched.getProfileImageURL))

}
