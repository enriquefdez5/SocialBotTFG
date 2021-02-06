package utilities.validations

import model.Action
import model.Action.{Action, getPossibleActions}
import model.exceptions.{EmptyStringException, IncorrectSizeListException, InvalidActionException, NullParamException, WrongParamValueException}
import org.apache.logging.log4j.scala.Logging

trait ValidationsUtil extends Logging {

  val maxDayValue = 6
  val maxHourValue = 23
  val minActionValue = 1
  val maxActionValue = 3


  def checkActionValue(action: Action): Unit = {
    val isValid: Boolean = isAValidAction(action, getPossibleActions)
    if ( !isValid) {
      throw InvalidActionException("The given action is not valid or implemented yet")
    }
  }

  private def isAValidAction(action: Action, possibleActionValues: Action.ValueSet): Boolean = {
    possibleActionValues.foreach(possibleAction => {
      if (possibleAction == action) {
        true
      }
    })
    false
  }

  def checkValue(value: Int, min: Int = 0, max: Int): Unit = {
    if (value < min || value > max) {
      throw WrongParamValueException("Param value is not valid")
    }
  }
  def checkNotNegativeInt(value: Int): Unit = {
    if (value < 0) {
      throw WrongParamValueException("Int param value can not be less than 0")
    }
  }
  def checkNotNegativeLong(value: Long): Unit = {
    if (value < 0) {
      throw WrongParamValueException("Long param value can not be less than 0")
    }
  }

  def checkNotNull(item: Any): Unit = {
    if (item == null) {
      throw NullParamException("Param can not be null")
    }
  }

  def checkNotEmptyString(string: String): Unit = {
    if (string == "" || string == " ") {
      throw EmptyStringException("The string can not be empty or blank")
    }
  }

  def checkNotEmptyList(list: Seq[Any]): Unit = {
    if (list.isEmpty) {
      throw IncorrectSizeListException("List can not be empty")
    }
  }

  def checkNotEmptyLinkedList(list: java.util.LinkedList[Int]): Unit = {
    if (list.isEmpty) {
      throw IncorrectSizeListException("List can not be empty")
    }
  }

}
