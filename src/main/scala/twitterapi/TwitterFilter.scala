package twitterapi

import com.vdurmont.emoji.EmojiParser
import org.apache.logging.log4j.scala.Logging
import utilities.validations.ValidationsUtil.checkNotEmptyList

object TwitterFilter extends Logging{


  /**
   * This function is used to clean tweets text.
   * @param tweets, Seq[String] to filter.
   * @return, Seq[String]. Filtered tweets.
   */
  def cleanTweets(tweets: Seq[String], language: Boolean): Seq[String] = {
    checkNotEmptyList(tweets)
    val commonFilteredTweets = commonClean(tweets)
    if (language) spanishFilter(tweets) else englishFilter(tweets)
  }

  private def commonClean(tweets: Seq[String]): Seq[String] = {
    tweets
      .filter( _.length > 80)               // Save only longer tweets
      .map{ _.toLowerCase}                  // Full lowercase for easier training
      .map{ _.replaceAll("=[A-Za-z0-9]* ", "")}   // Remove weird links
      .map{ _.replaceAll("\\w*\\u2026", "")}      // replace last word followed by "..." e.g adios...
      .map{ _.replaceAll("pic.*", "***PICTURE***")}      // Replace twitter pic links with a tag
      .map{ _.replaceAll("http[A-Za-z0-9-_:./?]*", "***LINK***") }
      // replace any link e.g http://google.com with a tag
      .map{ _.replaceAll( "(?:\\uD83C[\\uDF00-\\uDFFF])|(?:\\uD83D[\\uDC00-\\uDDFF])", "***EMOTE***" )}
      // change emotes into tag
      .map{ EmojiParser.removeAllEmojis }   // replace any other weird Emojis found e.g :)
  }
  private def spanishFilter(normalFilteredTweets: Seq[String]): Seq[String] = {
    // Replace spanish abbreviations like using "x" instead of "por". Useful for some people to better understand
    // the language they use.
    normalFilteredTweets
      .map{ _.replaceAll(" q ", " que ")}
      .map{ _.replaceAll(" d ", " de ")}
      .map{ _.replaceAll(" x ", " por ")}
  }


  private def englishFilter(normalFilteredTweets: Seq[String]): Seq[String] = {
    // Replace english abbreviations as it was done for spanish
    normalFilteredTweets
      .map{ _.replaceAll(" u ", " you ")}
      .map{ _.replaceAll(" ty ", " thank you ")}
      .map{ _.replaceAll( " ASAP ", " as soon as possible ")}
  }

  def markTweets(tweets: Seq[String]): Seq[String] = {
    checkNotEmptyList(tweets)
    // Adding line break after each tweet
    tweets.map{tweet => s"$tweet\n"}
  }
}
