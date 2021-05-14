package model

import model.NNActionItem.{maxActionValue, minActionValue}
import utilities.validations.ValidationsUtilTrait

/** Object that represents a type of action. */
object Action extends Enumeration with ValidationsUtilTrait {
  type Action = Value
  val POST, RT, REPLY = Value

  /**
   * @return Possible action values.
   */
  def getPossibleActions: ValueSet = {
    Action.values
  }

  /** Get an action from a given integer value.
   *
   * @param value Integer value.
   * @return Action value.
   */
  def getActionFromIntValue(value: Int): Action = {
    checkValue(value, minActionValue, maxActionValue)

    value match {
      case 1 => POST
      case 2 => RT
      case 3 => REPLY
    }
  }
}
