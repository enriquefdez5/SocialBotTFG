package twitterapi

import model.Post
import org.apache.logging.log4j.scala.Logging

object TwitterFilter extends Logging {

  /**
   * This function cleans tweets removing mentions(@Someone) and removing
   * links(http://www.anyWebPage.com/parameters or https...)
   * @param tweets. A Seq of Status(twitter4j class that contais tweet info) to be cleaned
   * @return A Seq of Strings containing the Status text without mentions or links.
   */
  def cleanTweets(tweets: Seq[Post]): Seq[String] ={
    val textFromTweets = tweets.map{ _.text }
    val textWithoutMentions = textFromTweets.map{ _.replaceAll("@\\w*", "") }
    val textWithoutMentionsNorLinks = textWithoutMentions.map{ _.replaceAll("http[A-Za-z0-9-_:./?]*", "") }

    logger.debug("--------- Text is printed without words like @someone on it ---------")
    logger.debug(textWithoutMentionsNorLinks.toString)

    textWithoutMentionsNorLinks    //return is omitted
  }

}
