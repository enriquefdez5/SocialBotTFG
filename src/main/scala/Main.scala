import twitterapi.{TwitterFilter, TwitterService}
import utilities.IOUtil

object Main {

  def main(args: Array[String]): Unit = {
    println("AIBehaviour twitter says hi!")

    //Twitter username where tweets will be search
    val twitterUser = "sanchezcastejon"

    //Get tweets from twitter
    val tweets = new TwitterService().getTweets(twitterUser)

    //Clean those tweets
    val tweetsFiltrados = new TwitterFilter().cleanTweets(tweets)

    //Search for FB post.

    //Clean FB text

    //It could be done with Instagram too
    //Search for FB post.

    //Clean FB text


    //Write text in file
    new IOUtil().writeDataOnAFile(tweetsFiltrados)

    //Append FB text to the training file.



    //Training model could be another MainMethod
    println("AIBheaviour twitter says good bye!")
  }
}
