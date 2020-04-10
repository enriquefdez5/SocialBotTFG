package facebookapi

import facebook4j.{FacebookFactory, Post, Reading}
import facebook4j.auth.AccessToken
import org.apache.logging.log4j.scala.Logging
import scala.collection.JavaConversions._

object FacebookService extends Logging {

  def getNewsFeed(): Seq[Post] = {

//    val cb = new ConfigurationBuilder()
//    cb.setDebugEnabled(true)
//      .setOAuthAppId("3536305023050599")
//      .setOAuthAppSecret("ddbf4581dd9dc4d171d45635129bb8d4")
//      .setOAuthAccessToken("EAAyQQCy3Y2cBADNrRtRedw9hQ481OfZAg2PZCXkN4EQFVQck6I8u34AFV2fHqxWc1LNgYPFqvZBzZA1ZAuUp77zMDE0t6UvCfimmuReNMykpEslEwJPPsi26ZCa7V8BFm1LJ0EZCbCdFoTR3SZCOXjSZAdgWoHYMkBOFzRwAvQzXpZBPDCM1X4qTJ4Xw2rkbjkL5U60qaCyL55xiJxcdWv3RcH")
//      .setOAuthPermissions("user_posts, public_profile")
//    val ff = new FacebookFactory(cb.build())
//    val facebook = ff.getInstance()




    // Generate facebook instance.
    val facebook = new FacebookFactory().getInstance()
    // Use default values for oauth app id.
    facebook.setOAuthAppId("3536305023050599"
      , "ddbf4581dd9dc4d171d45635129bb8d4")   // Get an access token from:
    // Copy and paste it below.
    val accessTokenString =
      "EAAyQQCy3Y2cBAAprnVhJdE7R2daGd83m8te1fcsqPWdPQZAp1cjPjNIP7j3Av0qKyHKp7oqhKAWku0E6I05KNIMqSiuxgfzRBHZAZBZB5WU4qzuVnRfQqqCsjGTM50QZCGCdaMiJkHxVe1BK0UbZBfnosZAIYgsav5ZBHrkNCqNkdZABipcsP6nQtB5LiHwNoHNZAo4rJM5ADb2OsKeHSFjZAdV";
    val at = new AccessToken(accessTokenString)
    // Set access token.
    facebook.setOAuthAccessToken(at)

    val accounts = facebook.getAccounts()
    val myPageAccount = accounts.get(0)
    val pageAccessToken = myPageAccount.getAccessToken

    facebook.setOAuthAccessToken(new AccessToken(pageAccessToken))
    facebook.setOAuthPermissions("manage_pages")



    // Set limit to 25 feeds.
    val searchLimit = 25
    val feeds = facebook.getFeed("pedro.sanchezperezcastejon",
      new Reading().limit(searchLimit))


//    // For all 25 feeds...
//    feeds.forEach{feed: Post =>
//      // Print out the message.
//      println(feed.getMessage())
//    }

    println(feeds.get(0).getMessage)

    feeds
  }
}
