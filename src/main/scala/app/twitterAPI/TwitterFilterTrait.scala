package app.twitterAPI

import com.vdurmont.emoji.EmojiParser
import utilities.validations.ValidationsUtilTrait

/** Trait that contains functions to filter string data. */
trait TwitterFilterTrait extends ValidationsUtilTrait {

  /** Clean tweets. It chooses whether to filter in spanish or english
   *
   * @param tweets. Tweets to filter.
   * @return a sequence of tweets filtered.
   */
  def cleanTweets(tweets: Seq[String], language: Boolean): Seq[String] = {
    checkNotEmptySeq(tweets)
    val commonFilteredTweets = commonClean(tweets)
    if (language) spanishFilter(commonFilteredTweets) else englishFilter(commonFilteredTweets)
  }

  /** Common clean for a string sequence.
   *
   * @param tweets. Tweets to filter
   * @return a sequence of tweets filtered.
   */
  private def commonClean(tweets: Seq[String]): Seq[String] = {
    tweets
      .filter( _.length > 80)
      .map{ _.toLowerCase}
      .map{ _.replaceAll("=[A-Za-z0-9]* ", "")}
      .map{ _.replaceAll("\\w*\\u2026", "")}
      .map{ _.replaceAll("pic.*", "picture")}
      .map{ _.replaceAll("http[A-Za-z0-9-_:./?]*", "link") }
      .map{ _.replaceAll( "(?:\\uD83C[\\uDF00-\\uDFFF])|(?:\\uD83D[\\uDC00-\\uDDFF])", "emote" )}
      .map{ EmojiParser.removeAllEmojis }
  }

  /** Spanish filter for a string sequence.
   *
   * @param tweets. Tweets to filter.
   * @return a sequence of tweets filtered.
   */
  private def spanishFilter(tweets: Seq[String]): Seq[String] = {
    tweets
      .map{ _.replaceAll(" q ", " que ")}
      .map{ _.replaceAll(" d ", " de ")}
      .map{ _.replaceAll(" x ", " por ")}
  }

  /** English filter for a string sequence.
   *
   * @param tweets. Tweets to filter.
   * @return a sequence of tweets filtered.
   */
  private def englishFilter(tweets: Seq[String]): Seq[String] = {
    tweets
      .map{ _.replaceAll(" u ", " you ")}
      .map{ _.replaceAll(" ty ", " thank you ")}
      .map{ _.replaceAll( " asap ", " as soon as possible ")}
  }

  /** Add a line break at the end of each tweets text.
   *
   * @param tweets. Tweets to add line break.
   * @return a sequence of tweets with a line break.
   */
  def addLineBreak(tweets: Seq[String]): Seq[String] = {
    checkNotEmptySeq(tweets)
    tweets.map{tweet => s"$tweet\n"}
  }
}
