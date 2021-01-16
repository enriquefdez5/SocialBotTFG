package twitterapi

import java.text.SimpleDateFormat
import java.util.Date

import model.Post
import org.apache.logging.log4j.scala.Logging
import twitter4j.Status

object TwitterFilter extends Logging{

  /**
   * This function cleans tweets removing mentions(@Someone) and removing
   * links(http://www.anyWebPage.com/parameters or https...)
   * @param tweets. A Seq of Status(twitter4j class that contais tweet info) to be cleaned
   * @return A Seq of Strings containing the Status text without mentions or links.
   */
  def cleanTweets(tweets: Seq[Post]): Seq[String] = {
    val textFromTweets = tweets.map{ _.text }
    val textWithoutMentions = textFromTweets.map{ _.replaceAll("@\\w*", "") }
    val textWithoutMentionsNorLinks = textWithoutMentions.map{ _.replaceAll("http[A-Za-z0-9-_:./?]*", "") }
    val textWithoutMentionsNorLinksNorRTs = textWithoutMentionsNorLinks.map{ _.replaceAll("RT : ", "")}
    val textWithoutMentionsNorLinksNorRTsNorHashtag = textWithoutMentionsNorLinksNorRTs.map{ _.replaceAll( "#", "")}

    textWithoutMentionsNorLinksNorRTsNorHashtag
  }

  def markTweets(tweets: Seq[String]): Seq[String] = {
    tweets.map(tweet => s"&$tweet%\n")
  }


  def obtainRts(tweets: Seq[Post]): Seq[String] = {
    val onlyRTs = tweets.filter(tweet => {
      isRetweet(tweet.retweetedStatus)
    })
    onlyRTs.map(tweet => {
      tweet.createdAt.toString
    })
  }

  def obtainReplys(tweets: Seq[Post]): Seq[String] = {
    val onlyReplys = tweets.filter(tweet => {
      isReply(tweet.getInReplyToUserId)
    })
    onlyReplys.map(tweet => {
      tweet.createdAt.toString
    })
  }

  private def normalizePost(tweet: Post): Int = {
    val isRetweeted = isRetweet(tweet.retweetedStatus)
    val isReplied = isReply(tweet.getInReplyToUserId)
    if (!isReplied && !isRetweeted ) 1 else 0
  }
  private def isRetweet(retweetStatus: Status): Boolean = {
    retweetStatus == null
  }

  private def isReply(replyId: Long): Boolean = {
    replyId != -1
  }

  private def normalizeDate(date: Date): Double = {
    val dayDateFormat = new SimpleDateFormat("EEE")
    val hourDateFormat = new SimpleDateFormat("HH")
    val minuteDateFormat = new SimpleDateFormat("mm")

    val dateDay = dayDateFormat.format(date)
    val dateHour: Int = hourDateFormat.format(date).toInt
    val dateMinutes: Int = minuteDateFormat.format(date).toInt

    val encodedDay: Double = encodeDay(dateDay)
    val encodedHour: Double = encodeHour(dateHour, dateMinutes)

    val normalizedDate: Double = encodedDay + encodedHour
    (math rint normalizedDate * 100) / 100
  }

  private def encodeHour(dateHour: Int, dateMinutes: Int): Double = {
    ((dateHour + (dateMinutes/60)*0.01) / 24) * 0.14
  }

  private def encodeDay(dateDay: String): Double = {
    dateDay match {
      case "lun." => 0/7.toDouble
      case "mar." => 1/7.toDouble
      case "mié." => 2/7.toDouble
      case "jue." => 3/7.toDouble
      case "vie." => 4/7.toDouble
      case "sáb." => 5/7.toDouble
      case "dom." => 6/7.toDouble
      case _ => -1
    }
  }


}
