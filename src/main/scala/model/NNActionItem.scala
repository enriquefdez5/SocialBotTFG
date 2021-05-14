package model

import java.util.Random
import org.apache.logging.log4j.scala.Logging

import app.twitterAPI.commandActions.{ActionCommandTrait, PostCommand, ReplyCommand, RtCommand}

import model.exceptions.{AIException, IncorrectCommandActionException}
import model.Action.{POST, REPLY, RT, getActionFromIntValue}
import model.Action.Action

import utilities.dates.DatesUtilTrait
import utilities.validations.ValidationsUtilTrait


/** Case class that represents an action to be executed on Twitter.
 *
 * @constructor Create a new NNActionItem with day of week, hour of day and action command.
 * @param dayOfWeek Day of week of the action. Must be an integer between 1 and 7
 * @param hourOfDay Hour of day of the action. Must be an integer between 0 and 23.
 * @param action Action command from the type of the action.
 */
case class NNActionItem(dayOfWeek: Int, hourOfDay: Int, action: ActionCommandTrait)

/** Object with functions to work with action items. */
object NNActionItem extends Logging with ValidationsUtilTrait with DatesUtilTrait {

  val maxDayValue = 7
  val maxHourValue = 23
  val minDayValue = 1
  val minHourValue = 0
  val maxActionValue = 3
  val minActionValue = 1

  /** Get an action item from date parameters.
   *
   * @param day Day of the action.
   * @param hour Hour of the action
   * @param actionValue Type of the action.
   * @param followedPostActionsCount Followed post actions count.
   * @param maxFollowedPostActions Maximum followed post actions.
   * @return Action item.
   */
  def buildNNActionItemFromDayHourAndAction(day: Int, hour: Int, actionValue: Int,
                                            followedPostActionsCount: Int, maxFollowedPostActions: Int): NNActionItem = {
    try {
      checkValue(hour, max = maxHourValue)
      checkValue(day, max = maxDayValue)
      checkValue(actionValue, minActionValue, maxActionValue)
      checkNotNegativeInt(followedPostActionsCount)
      checkNotNegativeInt(maxFollowedPostActions)
    }
    catch {
      case exception: AIException =>
        logger.error(exception.getMessage)
    }
    if (followedPostActionsCount >= maxFollowedPostActions) {
      val max = 3
      val min = 2
      val notPostRandomAction: Int = new Random().nextInt(max-min+1) + min  // +1 because nextInt(1) is only 0
      NNActionItem(day, hour, createCommandAction(getActionFromIntValue(notPostRandomAction)))
    }
    if (actionValue == 0) {
      NNActionItem(day, hour, createCommandAction(getActionFromIntValue(1)))
    }
    else {
      NNActionItem(day, hour, createCommandAction(getActionFromIntValue(actionValue)))
    }
  }


  private def createCommandAction(action: Action): ActionCommandTrait = {
    checkActionValue(action)

    action match {
      case POST => new PostCommand
      case RT => new RtCommand
      case REPLY => new ReplyCommand
      case _ => throw IncorrectCommandActionException()
    }
  }

  /** Get an action item from a Tweet.
   *
   * @param lastTweet Last tweet from the user.
   * @return Action item.
   */
  def statusToNNActionItem(lastTweet: StatusImpl): NNActionItem = {
    val calendar = getCalendarInstance
    calendar.setTime(lastTweet.createdAtDate)
    val day: Int = getCalendarDay(calendar)
    val hour: Int = getCalendarHour(calendar)
    val action: Action = getActionFromStatus(lastTweet)
    NNActionItem(day, hour, createCommandAction(action))
  }

  /** Get an action item from a string in a type and date format
   *
   * @param string String input from csv with type and date content.
   * @return ActionItem containing day, hour and type of action.
   */
  def stringToNNActionItem(string: String): NNActionItem = {
    try {
      val splitString = string.split(",")
      NNActionItem(splitString(0).toInt, splitString(1).toInt, createCommandAction(getActionFromString(splitString(2))))
    }
    catch {
      case _: Exception =>
        logger.error("Wrong input string format")
        System.exit(1)
        NNActionItem(0, 0, new PostCommand)
    }
  }

  private def getActionFromString(string: String): Action = {
    if (string == "1") {
      POST
    }
    else if (string == "2") {
      RT
    }
    else {
      REPLY
    }
  }

  private def getActionFromStatus(post: StatusImpl): Action = {
    if (post.currentUserRtId == 1) {
      RT
    }
    else if (post.getInReplyToUserId == 1) {
      REPLY
    }
    else {
      POST
    }
  }
}

