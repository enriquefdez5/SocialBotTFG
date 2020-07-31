package twitterapi

import com.vdurmont.emoji.EmojiParser
import model.Post
import org.apache.logging.log4j.scala.Logging

object TwitterFilter extends Logging{

  /**
   * This function cleans tweets removing mentions(@Someone) and removing
   * links(http://www.anyWebPage.com/parameters or https...)
   * @param tweets. A Seq of Status(twitter4j class that contais tweet info) to be cleaned
   * @return A Seq of Strings containing the Status text without mentions or links.
   */
  def cleanTweets(tweets: Seq[Post]): Seq[String] = {
    logger.debug("")
      tweets
        .map{ _.text }
        .map{ _.replaceAll("\\w*\\u2026", "")}   // replace last word followed by "..." e.g adios...
        .map{ _.replaceAll("http[A-Za-z0-9-_:./?]*", "") }    // replace any link e.g http://google.com
        .map{ _.replaceAll( "(?:\\uD83C[\\uDF00-\\uDFFF])|(?:\\uD83D[\\uDC00-\\uDDFF])", "" )}
        // replace unicode by blocks emotes
        .map{ EmojiParser.removeAllEmojis(_)}   // replace Emojis found e.g :)
  }

  def markTweets(tweets: Seq[String]): Seq[String] = {
    val filtered = tweets.filterNot(_.contains("RT @"))
    filtered
//      .map{tweet => s"& $tweet\n"}
      .map{tweet => s"$tweet\n"}

  }
}
