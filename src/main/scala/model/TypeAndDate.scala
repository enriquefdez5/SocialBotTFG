package model

import java.util.Random

import org.apache.logging.log4j.scala.Logging
import utilities.dates.datesUtil.{getCalendarDay, getCalendarHour, getCalendarInstance}

case class TypeAndDate(dayOfWeek: Int, hourOfDay: Int, action: Int)

object TypeAndDate extends Logging {

  /**
   * Function that builds a TypeAndDate object from a given day, hour and action and considering followed post actions
   * @param day, Int. Value of the day when the action will be executed. Must be between 0 and 6 inclusive.
   * @param hour, Int. Value of the hour when the action will be executed. Must be between 0 and 23 inclusive.
   * @param action, Int. Value of the type of action that will be executed. Must be between 1 and 3 inclusive.
   * @param followedPostActionsCount, Int. Count of followed post actions.
   * @param maxFollowedPostActions, Int. Number of maximum followed post actions.
   * @return TypeAndDate. Object containing the information for the next action to be executed.
   */
  def buildTypeAndDateFromDayHourAndAction(day: Int, hour: Int, action: Int,
                                           followedPostActionsCount: Int, maxFollowedPostActions: Int): TypeAndDate = {
    if (followedPostActionsCount >= maxFollowedPostActions) {
      val max = 3
      val min = 2
      val notPostRandomAction: Int = new Random().nextInt(max-min) + min
      TypeAndDate(day, hour, notPostRandomAction)
    }
    if (action == 0) {
      TypeAndDate(day, hour, 1)
    }
    else {
      TypeAndDate(day, hour, action)
    }
  }

  /**
   * Function that converts a given tweet as a Post object into a TypeAndDate object.
   * @param lastTweet, Post. Tweet given as a Post object which will be converted into a TypeAndDate object with the
   * day and hour of the given tweet and the action the tweet is.
   * @return TypeAndDate. The TypeAndDate built object.
   */
  def postToTypeAndDate(lastTweet: Post): TypeAndDate = {
    val calendar = getCalendarInstance
    calendar.setTime(lastTweet.createdAt)
    val day: Int = getCalendarDay(calendar)
    val hour: Int = getCalendarHour(calendar)
    val action: Int = getAction(lastTweet)
    TypeAndDate(day, hour, action)
  }

  /**
   * Function that returns an action type as an Integer based on Post received as param.
   * @param post to identify type of action from Twitter.
   * @return an Int object that represents the action type.
   */
  private def getAction(post: Post): Int = {
    // If it is a rt, returns rt value that is 2
    if (post.retweetedStatusUserId == 1) {
      2
    }
    // If it is a reply, returns reply value that is 3
    else if (post.getInReplyToUserId == 1) {
      3
    }
    // If it is not a reply or rt, it is a post, so returns the post value that is 1
    else {
      1
    }
  }
}

