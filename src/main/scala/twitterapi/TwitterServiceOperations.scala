package twitterapi

import java.util

import model.Post
import org.apache.logging.log4j.scala.Logging
import twitter4j.Status
import twitterapi.TwitterService.{csvSeparator, getAllActionsOrderedByDate}
import utilities.dates.datesUtil.{getCalendarInstance, groupTwitterActionsByDates}

import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
 * Object that contains the functions and operations the Twitter Service Object needs.
 */
object TwitterServiceOperations extends Logging {

  /**
   * Function that transforms a sequence of status (Seq[Status]) into  a sequence of Post objects (Seq[Post]).
   * @param tweets, Seq[Status]. A sequence of actions collected from the Twitter API.
   * @return a Seq[Post] that is the result of the transformation.
   */
  def statusesToPosts(tweets: Seq[Status]): Seq[Post] = {
    val calendar = getCalendarInstance
    tweets.map(it => {
      calendar.setTime(it.getCreatedAt)
      if (it.getRetweetedStatus != null) {
        Post(it.getText, calendar.getTime, it.getRetweetedStatus, it.getRetweetedStatus.getUser.getId, 0)
      }
      else {
        Post(it.getText, calendar.getTime, it.getRetweetedStatus, 0, it.getInReplyToStatusId)
      }
    })
  }

  /**
   * Function that recovers the last tweet not retweeted from a list of tweets.
   * @param tweets, Seq[Status]. A sequence of actions collected from the Twitter API.
   * @param idx, Int. Index to iterate over the list and find the last tweet not retweeted.
   * @return a Status. The last tweet not retweeded from the list.
   */
  @tailrec
  // TODO changed ResponseList[Status] to Seq[Status]. Check if it works
  def getLastTweetNotRetweeted(tweets: Seq[Status], idx: Int): Status = {
    if (tweets(idx).isRetweet) {
      getLastTweetNotRetweeted(tweets, idx + 1)
    }
    else {
      tweets(idx)
    }
  }

  /**
   * Function that recovers the last tweet not replied from a sequence of tweets.
   * @param tweets, Seq[Status]. A sequence of actions collected from the Twitter API.
   * @param idx, Int. Index to iterate over the list and find the last tweet not replied.
   * @return a Status. The last tweet not replied from the list.
   */
  @tailrec
  // TODO changed ResponseList[Status] to Seq[Status]. Check if it works
  def getLastTweetNotReplied(tweets: Seq[Status], idx: Int): Status = {
    if (tweets(idx).getInReplyToStatusId != 0) {  // TODO check if is != 0 or null or nan...
      getLastTweetNotReplied(tweets, idx + 1)
    }
    else {
      tweets(idx)
    }
  }

  /**
   * Function that obtains the most retweeted user id from a sequence of tweets containing posts, replies and retweets.
   * @param tweets, Seq[Post]. A sequence of actions obtained from the twitter API and then transformed into Post
   * objects. It contains a maximum of 3200 actions composed by posts, retweets and replies.
   * @return Long, an identifier of the most retweeted user.
   */
  def obtainMostRetweetedUserId(tweets: Seq[Post]): Long = {
    // Filter rts
    val onlyRts = tweets.filter(it => {
      isRetweet(it.retweetedStatus)
    })

    // Group by user id
    val onlyRetweetedGroupedByUserId = onlyRts.groupBy(it => {
      it.retweetedStatusUserId
    })

    // Find most retweeted one
    getMostInteractedUser(onlyRetweetedGroupedByUserId)
  }

  /**
   * Function that obtains the most replied user id from a list of tweets containing posts, replies and retweets.
   * @param tweets, Seq[Post]. A sequence of actions obtained from the twitter API and then transformed into Post
   * objects. It contains a maximum of 3200 actions composed by posts, retweets and replies.
   * @return Long, an identifier of the most replied user.
   */
  def obtainMostRepliedUserId(tweets: Seq[Post]): Long = {
    // Filter replies
    val onlyReplied = tweets.filter(it => {
      isReplied(it.getInReplyToUserId)
    })

    // Group by user id
    val onlyRepliedGroupedByUserId = onlyReplied.groupBy(it => {
      it.getInReplyToUserId
    })

    // Find most replied user id
    getMostInteractedUser(onlyRepliedGroupedByUserId)
  }

  /**
   * Function that finds the most interacted user from a list of post objects grouped by the user who wrote the tweet.
   * @param groupedTweets , Map[ Long, Seq[Post] ]. It is a map of Post objects grouped by user id.
   * User ids as keys.
   * Seq[Post] as values for each user id.
   * @return Long, the identifier of the most interacted user from the given list of post objects grouped by user
   *         identifier.
   */
  def getMostInteractedUser(groupedTweets: Map[Long, Seq[Post]]): Long = {
    val maxSize: Int = 0
    val mostInteractedUser: Long = 0
    val idx = 0
    val keys = groupedTweets.keys.toList
    getMostInteractedUserLoop(groupedTweets, maxSize, mostInteractedUser, keys, idx)
  }

  /**
   * Inner loop for finding the most interacted user from a list of Post objects grouped by user id.
   * @param groupedTweets, Map[Long, Seq[Post] ]. A list of Post objects grouped by the user id who interacted with
   * them.
   * @param maxSize, Int. The greatest amount of interactions. Used to obtain the most interacted user.
   * @param mostInteractedUser, Long. The user id of the user with the most interactions in each step of the loop.
   * @param keys, List[Long]. A list that contains the users ids. It is used to access the map object.
   * @param idx, Int. Index to iterate over the keys list and used to know when to stop the loop.
   * @return Long. The most interacted user id.
   */
  @tailrec
  private def getMostInteractedUserLoop(groupedTweets: Map[Long, Seq[Post]], maxSize: Int, mostInteractedUser: Long,
                                        keys: List[Long], idx: Int): Long = {
    if (idx < keys.length) {
      if (groupedTweets.get(keys(idx)).size > maxSize) {
        getMostInteractedUserLoop(groupedTweets, groupedTweets.get(keys(idx)).size, keys(idx), keys, idx + 1)
      }
      else {
        getMostInteractedUserLoop(groupedTweets, maxSize, mostInteractedUser, keys, idx + 1)
      }
    }
    else { mostInteractedUser }
  }

  /**
   * Function that obtains the maximum number of followed post actions from the list of historical actions of a user.
   * @param tweets, Seq[Post]. A sequence of actions obtained from the twitter API and then transformed into Post
   * objects. It contains a maximum of 3200 actions composed by posts, retweets and replies.
   * @param csvTweets, ArrayList[String]. A list of actions readed from the csv file. It contains a full user
   * historical composed by tweets and replies. It does not contain retweets.
   * @return Int. The computed maximum number of followed post actions.
   */
  def obtainMaxFollowedPostActions(tweets: Seq[Post], csvTweets: util.ArrayList[String]): Int = {
    val orderedDates: Seq[String] = getAllActionsOrderedByDate(tweets, csvTweets)
    val actions: Seq[String] = orderedDates.map(it => {
      it.split(csvSeparator)(1)
    })
    val postProportion: Double = getPostProportion(actions)
    (postProportion*10).toInt
  }

  /**
   * Function that starts the loop that computes de proportion of post actions in all the given actions.
   * @param actions, Seq[String]. Actions as String made of a date and a type of action.
   * @return Double. It is the post proportion in all the given actions.
   */
  private def getPostProportion(actions: Seq[String]): Double = {
    val postCount: Int = 0
    val totalCount: Int = 0
    val idx: Int = 0
    getPostProportionLoop(actions, postCount, totalCount, idx)
  }

  /**
   * Function that iterates as a loop over the given action sequence to compute the post action proportion.
   * @param actions, Seq[String]. Sequence of actions represented as strings containing date and type of action
   * separated by ','.
   * @param postCount, Int. Count of post actions at every step of the loop.
   * @param totalCount, Int. Count of total actinos at every step of the loop.
   * @param idx, Int. Index to iterate over the actions sequence and to know when to stops the loop.
   * @return Double. The proportion value of post actions over all the given actions.
   */
  @tailrec
  private def getPostProportionLoop(actions: Seq[String], postCount: Int, totalCount: Int, idx: Int): Double = {
    if (idx < actions.length) {
      if (actions.get(idx) == "1") {
        getPostProportionLoop(actions, postCount + 1, totalCount + 1, idx + 1)
      }
      else {
        getPostProportionLoop(actions, postCount, totalCount + 1, idx + 1)
      }
    }
    else {
      postCount / totalCount
    }
  }

  /**
   * Function that groups actions by date and then finds the max number of actions executed in any hour.
   * @param tweets, Seq[Post]. A sequence of actions obtained from the twitter API and then transformed into Post
   * objects. It contains a maximum of 3200 actions composed by posts, retweets and replies.
   * @param csvTweets, ArrayList[String]. A list of actions readed from the csv file. It contains a full user
   * historical composed by tweets and replies. It does not contain retweets.
   * @return Int. Returns the maximum number of actions per hour found.
   */
  def obtainMaxActionsPerHour(tweets: Seq[Post], csvTweets: util.ArrayList[String]): Int = {
    val dayOfYearAndHourMap = groupTwitterActionsByDates(tweets, csvTweets)
    obtainMaxNumberOfActions(dayOfYearAndHourMap)
  }

  /**
   * Function that obtains the maximum number of actions from a list of actions grouped by day of year and by hour.
   * @param dayOfYearAndHourMap, Iterable[Map[Int, Seq[String] ] ]. List of actions grouped by day of year and then by
   * hour.
   * @return Int. Returns the maximum number of actions per hour.
   */
  private def obtainMaxNumberOfActions(dayOfYearAndHourMap: Iterable[Map[Int, Seq[String]]]): Int = {
    var maxGroupLength: Int = 0
    dayOfYearAndHourMap.foreach(mapGroup => {
      mapGroup.foreach(it => {
        val dayAndHourGroupLength: Int = it._2.length
        if (dayAndHourGroupLength > maxGroupLength) {
          maxGroupLength = dayAndHourGroupLength
        }
      })
    })
    maxGroupLength
  }

  /**
   * Function that groups actions by date and then compute the mean of actions executed per hour.
   * @param tweets, Seq[Post]. A sequence of actions obtained from the twitter API and then transformed into Post
   * objects. It contains a maximum of 3200 actions composed by posts, retweets and replies.
   * @param csvTweets, ArrayList[String]. A list of actions readed from the csv file. It contains a full user
   * historical composed by tweets and replies. It does not contain retweets.
   * @return Int. Returns the mean actions per hour.
   */
  def obtainMeanActionsPerHour(tweets: Seq[Post], csvTweets: util.ArrayList[String]): Int = {
    val dayOfYearAndHourMap = groupTwitterActionsByDates(tweets, csvTweets)
    obtainMean(dayOfYearAndHourMap)
  }

  /**
   * Function that computes the mean value for a list of actions grouped by day of year and by hour.
   * @param dayOfYearAndHourMap, Iterable[Map[Int, Seq[String] ] ]. List of actions grouped by day of year and then by
   * hour.
   * @return Int. Returns the computed mean actions per hour.
   */
  private def obtainMean(dayOfYearAndHourMap: Iterable[Map[Int, Seq[String]]]): Int = {
    var groupLengthSum: Int = 0
    var numberOfElements: Int = 0
    dayOfYearAndHourMap.foreach(mapGroup => {
      mapGroup.foreach(it => {
        val dayAndHourGroupLength: Int = it._2.length
        groupLengthSum += dayAndHourGroupLength
        numberOfElements +=1
      })
    })
    groupLengthSum / numberOfElements
  }

  //  private def obtainMeanLoop(iterable: Iterable[Map[Int, Seq[String]]], idx: Int, sum: Int): Unit = {
  //    if (idx >= iterable.size) {
  //      sum
  //    }
  //    else {
  //      val iterableToList = iterable.toList
  //      val anotherIdx = 20
  //      val map = iterableToList(anotherIdx)
  //      val mapListKeys = map.keys.toList
  //      val idx = 0
  //      val sum = obtainSumInnerLoop(map, mapListKeys, idx, 0)
  //    }
  //  }
  //
  //  @tailrec
  //  private def obtainSumInnerLoop(mapList: Map[Int, Seq[String]], keys: util.List[Int], idx: Int, sum: Int): Int = {
  //    if (idx < keys.length) {
  //      obtainSumInnerLoop(mapList, keys, idx+1, sum + mapList.get(keys(idx)).size)
  //    }
  //    else {
  //      sum
  //    }
  //  }


  /**
   * Function that filters a sequence of tweets into a sequence of retweets and maps that sequence into a sequence of
   * strings composed with those tweets' dates
   * @param tweets, Seq[Post]. A sequence of actions obtained from the twitter API and then transformed into Post
   * objects. It contains a maximum of 3200 actions composed by posts, retweets and replies.
   * @return Seq[String]. Converted sequence of tweets into seq of strings composed by dates.
   */
  def obtainRtsDates(tweets: Seq[Post]): Seq[String] = {
    val onlyRTs = tweets.filter(tweet => {
      isRetweet(tweet.retweetedStatus)
    })
    onlyRTs.map(tweet => {
      tweet.createdAt.toString
    })
  }

  /**
   * Function that checks if a Status is a Retweet action or not.
   * @param retweetStatus, Status. Status to check if it is a retweet action or not
   * @return Boolean. True if it a Retweet action and false if it is not.
   */
  private def isRetweet(retweetStatus: Status): Boolean = {
    retweetStatus == null
  }

  /**
   * Function that checks if a Status is a Reply action or not.
   * @param replyId, Long. Status id to check if it is a reply action or not.
   * @return Boolean. True if it is a reply action and false if it is not.
   */
  private def isReplied(replyId: Long): Boolean = {
    replyId != -1
  }




}
