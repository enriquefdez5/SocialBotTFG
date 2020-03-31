package twitterapi

//util libs
import java.io.FileInputStream
import java.util.Properties
import utilities.Logger
import org.apache.logging.log4j.Level

//scala libs
import scala.collection.JavaConversions._
import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

//app libs
import model.{Plataforma, Post, User, UserInfo, UserStats, ProfileImg}

//twitter4j libs
import twitter4j.{Paging, Status, TwitterException, TwitterFactory, Twitter}
import twitter4j.conf.ConfigurationBuilder

object TwitterService {

  //Read credentials file
  val credentials: Properties = new Properties()
  credentials.load(new FileInputStream("src/main/resources/credentials.properties"))

  //Read properties file
  val properties: Properties = new Properties()
  properties.load(new FileInputStream("src/main/resources/config.properties"))

  def getTweets(userName: String): ListBuffer[Post] ={
    val tweets = ListBuffer[Status]()

    val pagInit = 1
    val totalTweetsGathered = 0

    //Obtain twitter client
    val twitter = getTwitterClient

    //Get profile data
    val userSearched = twitter.showUser(userName)
    Logger.log(Level.DEBUG, s"Showing $userName profile Info")

    //Create profile just in case I need it for NN or something
    val user = createUser(userSearched)

    Logger.log(Level.DEBUG, "User searched ${userSearched.toString}")

    //Get ~3200 user tweets
    recursiveWhileLoop(twitter,totalTweetsGathered, pagInit, userName, user, tweets )
    tweets.map(tweet => Post( tweet.getId, user, tweet.getText, tweet.getCreatedAt.toString, tweet.getRetweetCount,
      tweet.getFavoriteCount, tweet.getGeoLocation, tweet.isRetweeted, tweet.isFavorited, Plataforma.twitter))
  }

  /**
   * Function for collecting tweets
   * @param twitter, client to request operation
   * @param totalTweetsGathered, amount of tweets collected in each call, max is ~3200
   * @param pageInit, number of page from which tweets are collected
   * @param userName, user profile where to search
   * @param user, app object that represents a user profile
   * @param tweets, list of tweets collected
   */
  @tailrec
  private def recursiveWhileLoop(twitter: Twitter, totalTweetsGathered: Int, pageInit: Int, userName: String, user: User,
                         tweets: ListBuffer[Status]): Unit ={
    if (totalTweetsGathered < properties.getProperty("maxNumberTweetsAllowed").toInt){
      var newTweetsCall = 0
      try {
        val page = new Paging(pageInit+1, properties.getProperty("gatheringTweetsPageSize").toInt)
        tweets.addAll(twitter.getUserTimeline(userName, page))
        val newTweets = twitter.getUserTimeline(userName, page).size()

        Logger.log(Level.DEBUG, s"Gathered ${twitter.getUserTimeline(userName, page).size()} tweets" )

        newTweetsCall += newTweets

        if (tweets.size() == tweets.size + newTweets){
          //It could return something or at least break while loop
          throw new TwitterException("There are no more new tweets to collect")
        }
      }catch{
        case e: TwitterException => e.printStackTrace();
      }
      recursiveWhileLoop(twitter,totalTweetsGathered+newTweetsCall, pageInit, userName, user, tweets)
    }
  }

  /**
   * Method for creating a twitter client instane
   * @return Twitter, a twitter client instance
   */
  def getTwitterClient: Twitter ={
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey(credentials.getProperty("ConsumerTokenKey"))
      .setOAuthConsumerSecret(credentials.getProperty("ConsumerTokenKeySecret"))
      .setOAuthAccessToken(credentials.getProperty("AccessTokenKey"))
      .setOAuthAccessTokenSecret(credentials.getProperty("AccessTokenKeySecret"))
    val tf = new TwitterFactory(cb.build)
    val twitter = tf.getInstance
    twitter
  }


  /**
   *  Method for creating an object with user info needed
   * @param userSearched with info needed to create the app User
   * @return User, app user object
   */
  private def createUser(userSearched: twitter4j.User):User = User(id = userSearched.getId, strId = userSearched.getId.toString,
    userInfo = UserInfo(name = userSearched.getName,
      description = userSearched.getDescription, createdAt = userSearched.getCreatedAt.toString,
      location = userSearched.getLocation, profileUrl = userSearched.getURL),
    userStats = UserStats(followers = userSearched.getFollowersCount, followings = userSearched.getFriendsCount,
      isProtected = userSearched.isProtected, isVerified = userSearched.isVerified),
    profileImg = ProfileImg(defaultProfileImgUrl = userSearched.isDefaultProfileImage,
      profileImgUrl = userSearched.getProfileImageURL))

}
