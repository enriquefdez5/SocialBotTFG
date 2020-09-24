package twitterapi

import com.vdurmont.emoji.EmojiParser
import org.apache.logging.log4j.scala.Logging

object TwitterFilter extends Logging{

  /**
   * This function cleans tweets removing mentions(@Someone) and removing
   * links(http://www.anyWebPage.com/parameters or https...)
   * @param tweets. A Seq of Status(twitter4j class that contais tweet info) to be cleaned
   * @return A Seq of Strings containing the Status text without mentions or links.
   */
  def cleanTweets(tweets: Seq[String]): Seq[String] = {
    logger.debug("")
      tweets
        .filter( _.length > 80)               // Save longer tweets
        .filterNot(_.contains("RT @"))        // Remove retweets
        .map{ _.toLowerCase}                  // Full lowercase for easier training
        .map{ _.replaceAll("=[A-Za-z0-9]* ", "")}   // Remove weird links
        .map{ _.replaceAll("\\w*\\u2026", "")}
        // replace last word followed by "..." e.g adios...
        .map{ _.replaceAll("http[A-Za-z0-9-_:./?]*", "") }
        // replace any link e.g http://google.com
        .map{ _.replaceAll( "(?:\\uD83C[\\uDF00-\\uDFFF])|(?:\\uD83D[\\uDC00-\\uDDFF])", "" )}
        // replace unicode by blocks emotes
        .map{ EmojiParser.removeAllEmojis}   // replace Emojis found e.g :)
        .map{ _.replaceAll("pic.*", "")}      // Remove twitter pic links
        // Remove wrong language like using "x" instead of "por". Useful for Pedro Sanchez testing tweets. Same for
        // other people.
        .map{ _.replaceAll(" q ", " que ")}
        .map{ _.replaceAll(" d ", " de ")}
        .map{ _.replaceAll(" x ", " por ")}
  }

  def markTweets(tweets: Seq[String]): Seq[String] = {
    // Adding line break after each tweet
    tweets.map{tweet => s"$tweet\n"}
  }
}
