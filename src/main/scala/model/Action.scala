package model

import model.TypeAndDate.{maxActionValue, minActionValue}
import utilities.validations.ValidationsUtil

/**
 *  Enumeration object containing the three types of Actions available.
 */

object Action extends Enumeration with ValidationsUtil {
  type Action = Value
  val POST, RT, REPLY = Value

  def getPossibleActions: ValueSet = {
    Action.values
  }

  def getActionFromIntValue(value: Int): Action = {
    checkValue(value, minActionValue, maxActionValue)

    value match {
      case 1 => POST
      case 2 => RT
      case 3 => REPLY
    }
  }
}
