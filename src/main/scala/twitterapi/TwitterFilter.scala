package twitterapi

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
    tweets.map{ _.text }.map{ _.replaceAll("@\\w*", "") }
      .map{ _.replaceAll("http[A-Za-z0-9-_:./?]*", "") }
      .map{ _.replaceAll("RT : ", "")}
      .map{ _.replaceAll( "#", "")}
  }

  def markTweets(tweets: Seq[String]): Seq[String] = {
    tweets.map(tweet => s"&$tweet%\n")
  }

}
