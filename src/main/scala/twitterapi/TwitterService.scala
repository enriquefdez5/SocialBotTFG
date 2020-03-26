package twitterapi

//java libs
import java.util

//scala libs
import scala.collection.JavaConversions._

//app libs
import model.{Plataforma, Post, User}

//twitter4j libs
import twitter4j.{Paging, Status, TwitterException, TwitterFactory}
import twitter4j.conf.ConfigurationBuilder
class TwitterService {

  def getTweets(userName: String): Seq[Status] ={
    val debug = false

    val tweets = new util.ArrayList[Status]()
    val userPost = new util.ArrayList[Post]()

    var paginit = 1
    var totalTweetsGathered = 0

    //Create twitter client
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey("*****************")
      .setOAuthConsumerSecret("*****************")
      .setOAuthAccessToken("*****************")
      .setOAuthAccessTokenSecret("*****************")
    val tf = new TwitterFactory(cb.build)
    val twitter = tf.getInstance

    //Get profile data
    val userSearched = twitter.showUser(userName)
    if (debug) {
      System.out.println("Showing " + userName + " profile Info")
    }

    //Create profile just in case I need it for NN or something
    val user = new User(id = userSearched.getId, strId = userSearched.getId.toString, name = userSearched.getName,
      description = userSearched.getDescription, createdAt = userSearched.getCreatedAt.toString,
      followers = userSearched.getFollowersCount, followings = userSearched.getFriendsCount,
      location = userSearched.getLocation, isProtected = userSearched.isProtected,
      isVerified = userSearched.isVerified, profileUrl = userSearched.getURL,
      profileImgUrl = userSearched.getProfileImageURL, defaultProfileImgUrl = userSearched.isDefaultProfileImage)
    if (debug){
      System.out.println(userSearched.toString)
    }

    //Get ~3200 user tweets
    while (totalTweetsGathered < 300) {
      try {
        val size = tweets.size()
        paginit+=1
        val page = new Paging(paginit, 200)
        tweets.addAll(twitter.getUserTimeline(userName, page))
        val newTweets = twitter.getUserTimeline(userName, page).size()

        if (debug) {
          System.out.println("***********************************************")
          System.out.println("Gathered " + twitter.getUserTimeline(userName, page).size() + " tweets")
        }

        totalTweetsGathered += newTweets

        //Save each tweet colledted as app model, so it can be used for training data
        for (tweet <- tweets){
          val post = new Post( tweet.getId, user, tweet.getText, tweet.getCreatedAt.toString, tweet.getRetweetCount,
            tweet.getFavoriteCount, tweet.getGeoLocation, tweet.isRetweeted, tweet.isFavorited, Plataforma.twitter)
          userPost.add(post)
        }
        if (tweets.size() == size){
          //It could return something or at least break while loop
        }
      }catch{
        case e: TwitterException => e.printStackTrace();
      }
    }
    tweets
  }
}
