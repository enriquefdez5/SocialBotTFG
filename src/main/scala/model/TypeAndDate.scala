package model

// java imports
import java.util.Random

import model.exceptions.IncorrectCommandActionException
import utilities.validations.ValidationsUtil.{checkActionValue, checkNotNegativeInt, checkNotNegativeLong, checkNotNull, checkValue, maxActionValue, maxDayValue, maxHourValue, minActionValue}

// Command imports
import twitterapi.commandActions.{PostCommand, RtCommand, ReplyCommand, ActionCommand}

import model.Action.{POST, REPLY, RT, getActionFromIntValue}


// model imports
import model.Action.Action

// logging imports
import org.apache.logging.log4j.scala.Logging

// dates imports
import utilities.dates.datesUtil.{getCalendarDay, getCalendarHour, getCalendarInstance}

/**
 * Case class that represents an action to be executed on Twitter the dayOfWeek day at hourOfDay hour. The action to
 * be executed will be from an Action Type.
 * @param dayOfWeek, Int. Int value representing the day of a week when the action must be executed.
 * 0 for Saturday and 6 for Friday
 * @param hourOfDay, Int. Int value representing the hour of a day when the action must be executed.
 * 0 for 00:00 and 23 for 23:00.
 * @param action, Action. Action enumeration value representing the type of action that must be executed.
 * 1 for POST, 2 for RT and 3 for REPLY
 */
case class TypeAndDate(dayOfWeek: Int, hourOfDay: Int, action: ActionCommand)

object TypeAndDate extends Logging {

  /**
   * Function that builds a TypeAndDate object from a given day, hour and action and considering followed post actions
   * @param day, Int. Value of the day when the action will be executed. Must be between 0 and 6 inclusive.
   * @param hour, Int. Value of the hour when the action will be executed. Must be between 0 and 23 inclusive.
   * @param actionValue, Int. Value of the type of action that will be executed. Must be between 1 and 3 inclusive.
   * @param followedPostActionsCount, Int. Count of followed post actions.
   * @param maxFollowedPostActions, Int. Number of maximum followed post actions.
   * @return TypeAndDate. Object containing the information for the next action to be executed.
   */
  def buildTypeAndDateFromDayHourAndAction(day: Int, hour: Int, actionValue: Int,
                                           followedPostActionsCount: Int, maxFollowedPostActions: Int): TypeAndDate = {

    checkNotNull(hour)
    checkValue(hour, max = maxHourValue)
    checkNotNull(day)
    checkValue(day, max = maxDayValue)
    checkNotNull(actionValue)
    checkValue(actionValue, minActionValue, maxActionValue)
    checkNotNull(followedPostActionsCount)
    checkNotNegativeInt(followedPostActionsCount)
    checkNotNull(maxFollowedPostActions)
    checkNotNegativeInt(maxFollowedPostActions)

    if (followedPostActionsCount >= maxFollowedPostActions) {
      val max = 3
      val min = 2
      val notPostRandomAction: Int = new Random().nextInt(max-min) + min
      TypeAndDate(day, hour, createCommandAction(getActionFromIntValue(notPostRandomAction)))
    }
    if (actionValue == 0) {
      TypeAndDate(day, hour, createCommandAction(getActionFromIntValue(1)))
    }
    else {
      TypeAndDate(day, hour, createCommandAction(getActionFromIntValue(actionValue)))
    }
  }


  private def createCommandAction(action: Action): ActionCommand = {
    checkNotNull(action)
    checkActionValue(action)

    action match {
      case POST => new PostCommand
      case RT => new RtCommand
      case REPLY => new ReplyCommand
      case _ => throw IncorrectCommandActionException()
    }
  }
  /**
   * Function that converts a given tweet as a Post object into a TypeAndDate object.
   * @param lastTweet, Post. Tweet given as a Post object which will be converted into a TypeAndDate object with the
   * day and hour of the given tweet and the action the tweet is.
   * @return TypeAndDate. The TypeAndDate built object.
   */
  def postToTypeAndDate(lastTweet: Post): TypeAndDate = {
    validatePostObject(lastTweet)

    val calendar = getCalendarInstance
    calendar.setTime(lastTweet.createdAt)
    val day: Int = getCalendarDay(calendar)
    val hour: Int = getCalendarHour(calendar)
    val action: Action = getActionFromPostObject(lastTweet)
    TypeAndDate(day, hour, createCommandAction(action))
  }

  /**
   * Private function that checks a Post object
   * @param post, Post. Post object which be validatedto be validated
   */
  private def validatePostObject(post: Post): Unit = {
    checkNotNull(post)
    checkNotNull(post.getInReplyToUserId)
    checkNotNegativeLong(post.getInReplyToUserId)
    checkNotNull(post.retweetedStatus)
    checkNotNull(post.retweetedStatusUserId)
    checkNotNegativeLong(post.retweetedStatusUserId)
    checkNotNull(post.createdAt)
  }
  /**
   * Function that returns an action type as an Integer based on Post received as param.
   * @param post to identify type of action from Twitter.
   * @return an Int object that represents the action type.
   */
  private def getActionFromPostObject(post: Post): Action = {
    validatePostObject(post)

    // If it is a rt, returns rt value that is 2
    if (post.retweetedStatusUserId == 1) {
      RT
    }
    // If it is a reply, returns reply value that is 3
    else if (post.getInReplyToUserId == 1) {
      REPLY
    }
    // If it is not a reply or rt, it is a post, so returns the post value that is 1
    else {
      POST
    }
  }
}

