package twitterapi

//util libs
import java.io.FileInputStream
import java.util.Properties

import model.{UserInfo, UserStats}
import org.apache.logging.log4j.{LogManager, Logger}

import scala.annotation.tailrec
import scala.collection.convert.Wrappers.MutableSeqWrapper
import scala.collection.mutable.ListBuffer

//scala libs
import scala.collection.JavaConversions._

//app libs
import model.{Plataforma, Post, User, ProfileImg}

//twitter4j libs
import twitter4j.{Paging, Status, TwitterException, TwitterFactory, Twitter}
import twitter4j.conf.ConfigurationBuilder

object TwitterService {

  val logger: Logger = LogManager.getLogger()

  //Read credentials file
  val credentials: Properties = new Properties()
  credentials.load(new FileInputStream("src/main/resources/credentials.properties"))

  //Read properties file
  val properties: Properties = new Properties()
  properties.load(new FileInputStream("src/main/resources/config.properties"))

  def getTweets(userName: String): Seq[Status] ={
//    val tweets = Seq[Status]()
    val tweets = ListBuffer[Status]()
    val userPost = ListBuffer[Post]()

    val paginit = 1
    val totalTweetsGathered = 0

    //Obtain twitter client
    val twitter = getTwitterClient

    //Get profile data
    val userSearched = twitter.showUser(userName)
    logger.debug(s"Showing $userName profile Info")

    //Create profile just in case I need it for NN or something
    val user = createUser(userSearched)

    logger.debug(userSearched.toString)

    //Get ~3200 user tweets
    recursiveWhileLoop(twitter,totalTweetsGathered, paginit, userName, user, userPost, tweets )
//    while (totalTweetsGathered < 3000) {
//      try {
//        val size = tweets.size
//        paginit+=1
//        val page = new Paging(paginit, 200)
//        tweets.addAll(twitter.getUserTimeline(userName, page))
//        val newTweets = twitter.getUserTimeline(userName, page).size()
//
//        logger.debug("Gathered " + twitter.getUserTimeline(userName, page).size() + " tweets")
//
//        totalTweetsGathered += newTweets
//
//        //Save each tweet colledted as app model, so it can be used for training data
//        for (tweet <- tweets){
//          val post = Post( tweet.getId, user, tweet.getText, tweet.getCreatedAt.toString, tweet.getRetweetCount,
//            tweet.getFavoriteCount, tweet.getGeoLocation, tweet.isRetweeted, tweet.isFavorited, Plataforma.twitter)
//          userPost.add(post)
//        }
//        if (tweets.size() == size){
//          //It could return something or at least break while loop
//        }
//      }catch{
//        case e: TwitterException => e.printStackTrace();
//      }
//    }
    tweets
  }

  /**
   * Function for collecting tweets
   * @param twitter, client to request operation
   * @param totalTweetsGathered, amount of tweets collected in each call, max is ~3200
   * @param paginit, number of page from which tweets are collected
   * @param userName, user profile where to search
   * @param user, app object that represents a user profile
   * @param userPost, list of app object
   * @param tweets, list of tweets collected
   */
  @tailrec
  def recursiveWhileLoop(twitter: Twitter, totalTweetsGathered: Int, paginit: Int, userName: String, user: User,
                         userPost: ListBuffer[Post], tweets: ListBuffer[Status]): Unit ={
    var paginitLocal: Int = paginit
    var totalTweetsGatheredLocal: Int = totalTweetsGathered

    if (totalTweetsGatheredLocal < properties.getProperty("maxNumberTweetsAllowed").toInt){
      try {
        val size = tweets.size
        paginitLocal+=1
        val page = new Paging(paginit, properties.getProperty("gatheringTweetsPageSize").toInt)
        tweets.addAll(twitter.getUserTimeline(userName, page))
        val newTweets = twitter.getUserTimeline(userName, page).size()

        logger.debug(s"Gathered ${twitter.getUserTimeline(userName, page).size()} tweets")

        totalTweetsGatheredLocal += newTweets

        //Save each tweet colledted as app model, so it can be used for training data
        val index = 0
        addToPostsList(index,tweets, userPost, user)
//        for (tweet <- tweets){
//          userPost.add(Post( tweet.getId, user, tweet.getText, tweet.getCreatedAt.toString, tweet.getRetweetCount,
//            tweet.getFavoriteCount, tweet.getGeoLocation, tweet.isRetweeted, tweet.isFavorited, Plataforma.twitter))
//        }
        if (tweets.size() == size){
          //It could return something or at least break while loop
          throw new TwitterException("There are no more new tweets to collect")
        }
      }catch{
        case e: TwitterException => e.printStackTrace();
      }
      recursiveWhileLoop(twitter,totalTweetsGatheredLocal, paginitLocal, userName, user, userPost, tweets)
    }
  }


  @tailrec
  def addToPostsList(index: Int, tweets: ListBuffer[Status], userPost: ListBuffer[Post], user: User){
    var indexLocal = index
    if (indexLocal < tweets.size-1){
      val tweet = tweets.get(indexLocal)
      userPost.add(Post( tweet.getId, user, tweet.getText, tweet.getCreatedAt.toString, tweet.getRetweetCount,
                  tweet.getFavoriteCount, tweet.getGeoLocation, tweet.isRetweeted, tweet.isFavorited, Plataforma.twitter))
      indexLocal+=1
      addToPostsList(indexLocal, tweets,userPost, user)
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
  def createUser(userSearched: twitter4j.User):User = User(id = userSearched.getId, strId = userSearched.getId.toString,
    userInfo = UserInfo(name = userSearched.getName,
      description = userSearched.getDescription, createdAt = userSearched.getCreatedAt.toString,
      location = userSearched.getLocation, profileUrl = userSearched.getURL),
    userStats = UserStats(followers = userSearched.getFollowersCount, followings = userSearched.getFriendsCount,
      isProtected = userSearched.isProtected, isVerified = userSearched.isVerified),
    profileImg = ProfileImg(defaultProfileImgUrl = userSearched.isDefaultProfileImage,
      profileImgUrl = userSearched.getProfileImageURL))

}
