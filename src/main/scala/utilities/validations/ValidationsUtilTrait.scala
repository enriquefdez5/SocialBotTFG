package utilities.validations

import model.Action.{Action, getPossibleActions}
import model.NNActionItem.{maxDayValue, maxHourValue, minDayValue, minHourValue}
import model.exceptions.{EmptyStringException, IncorrectSizeListException, InvalidActionException, NoneParamException, WrongParamValueException}
import org.apache.logging.log4j.scala.Logging

trait ValidationsUtilTrait extends Logging {


  def checkActionValue(action: Action): Unit = {
    if (!getPossibleActions.toList.contains(action)) {
      throw InvalidActionException("The given action is not valid or implemented yet")
    }
  }
  def checkValue(value: Int, min: Int = 0, max: Int): Unit = {
    if (value < min || value > max) {
      throw WrongParamValueException("Param value is not valid")
    }
  }
  def checkDayOfWeek(day: Int): Int = {
    if (day < minDayValue) {
      minDayValue
    }
    else if (day > maxDayValue) {
      maxDayValue
    }
    else {
      day
    }
  }
  def checkHourOfDay(hour: Int): Int = {
    if (hour < minHourValue) {
      minHourValue
    }
    else if (hour > maxHourValue) {
      maxHourValue
    }
    else {
      hour
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
    if (item == None) {
      throw NoneParamException("Param can not be None")
    }
  }
  def checkNotEmptyString(string: String): Unit = {
    if (string == "" || string == " ") {
      throw EmptyStringException("The string can not be empty or blank")
    }
  }
  def checkNotEmptySeq(seq: Seq[Any]): Unit = {
    if (seq.isEmpty) {
      throw IncorrectSizeListException("List can not be empty")
    }
  }
  def checkNotEmptyLinkedList(list: java.util.LinkedList[Int]): Unit = {
    if (list.isEmpty) {
      throw IncorrectSizeListException("List can not be empty")
    }
  }

}
