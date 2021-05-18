package model

import java.util.Random

import app.twitterAPI.commandActions.{ActionCommandTrait, PostCommand, ReplyCommand, RtCommand}
import model.Action.{Action, POST, REPLY, RT, getActionFromIntValue}
import model.exceptions.{AIException, IncorrectCommandActionException}
import org.apache.logging.log4j.scala.Logging
import utilities.dates.DatesUtilTrait
import utilities.validations.ValidationsUtilTrait

class NNActionItem {
  var day: Option[Int] = None
  var hour: Option[Int] = None
  var commandTrait: Option[ActionCommandTrait] = None
}

/** Object with functions to work with action items. */
object NNActionItem extends Logging with ValidationsUtilTrait with DatesUtilTrait {

  def apply(day: Option[Int], hour: Option[Int], commandTrait: Option[ActionCommandTrait]): NNActionItem = {
    val nNActionItem = new NNActionItem
    nNActionItem.day = day
    nNActionItem.hour = hour
    nNActionItem.commandTrait = commandTrait
    nNActionItem
  }


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
      NNActionItem(Some(day), Some(hour), Some(createCommandAction(getActionFromIntValue(notPostRandomAction))))
    }
    if (actionValue == 0) {
      NNActionItem(Some(day), Some(hour), Some(createCommandAction(getActionFromIntValue(1))))
    }
    else {
      NNActionItem(Some(day), Some(hour), Some(createCommandAction(getActionFromIntValue(actionValue))))
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
    NNActionItem(Some(day), Some(hour), Some(createCommandAction(action)))
  }

  /** Get an action item from a string in a type and date format
   *
   * @param string String input from csv with type and date content.
   * @return ActionItem containing day, hour and type of action.
   */
  def stringToNNActionItem(string: String): NNActionItem = {
    try {
      val splitString = string.split(",")
      NNActionItem(Some(splitString(0).toInt), Some(splitString(1).toInt), Some(createCommandAction
      (getActionFromString(splitString(2)))))
    }
    catch {
      case _: Exception =>
        logger.error("Wrong input string format")
        System.exit(1)
        NNActionItem(Some(0), Some(0), Some(new PostCommand))
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