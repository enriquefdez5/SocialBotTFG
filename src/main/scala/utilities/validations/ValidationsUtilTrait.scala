package utilities.validations

import java.util

import model.Action.{Action, getPossibleActions}
import model.NNActionItem.{maxDayValue, maxHourValue, minDayValue, minHourValue}
import model.exceptions.{EmptyStringException, IncorrectSizeListException, InvalidActionException, WrongParamValueException}

/** Trait that contains functionality to make validation operations. */
trait ValidationsUtilTrait {

  /** Check action object value is one of possible Action values.
   *
   * @param action Action object.
   * @throws InvalidActionException if it is not a valid action object.
   */
  def checkActionValue(action: Action): Unit = {
    if (!getPossibleActions.toList.contains(action)) {
      throw InvalidActionException("The given action is not valid or implemented yet")
    }
  }

  /** Check value is greater than a value and less than another value.
   *
   * @param value Value to check.
   * @param min Minimum value.
   * @param max Maximum value.
   * @throws WrongParamValueException if it is not between minimum and maximum values.
   */
  def checkValue(value: Int, min: Int = 0, max: Int): Unit = {
    if (value < min || value > max) {
      throw WrongParamValueException("Param value is not valid")
    }
  }

  /** Check day of week value is correct.
   *
   * @param day Day value to check.
   * @return Minimum day value if day value to check is less than minimum day value.
   *         Maximum day value if day value to check is greater than maximum day value.
   *         Day value if day value to check is between minimum and maximum day value.
   */
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

  /** Check if hour of day is correct.
   *
   * @param hour Hour value to check.
   * @return Minimum hour value if hour value to check is less than minimum hour value.
   * @return Maximum hour value if hour value to check is greater than Maximum hour value.
   * @return Hour value if hour value to check is between minimum and maximum hour value.
   */
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


  /** Check day value for a given month.
   *
   * @param day Day value to check.
   * @param maxDayOfMonth Maximum day value for the given month.
   * @return Day value if day value to check is greater than minimum day value and less than maximum day for the
   *         given month.
   * @throws WrongParamValueException if day value to check is less than the minimum day value or greater than
   *                                  maximum day value.
   */
  def checkDay(day: Int, maxDayOfMonth: Int): Int = {
    if (day < minDayValue) {
      throw WrongParamValueException("Day value can not be less than 1")
    }
    if (day > maxDayOfMonth) {
      throw WrongParamValueException("Day value can not be more than " + maxDayOfMonth + " for that month")
    }
    else {
      day
    }
  }

  /** Check month value.
   *
   * @param month Month value to check.
   * @return Month value if it is greater than minimum month value and less than maximum month value.
   * @throws WrongParamValueException if month value is less than minimum month value or greater than maximum month
   *                                  value.
   */
  def checkMonth(month: Int): Int = {
    val minMonthValue = 0
    val maxMonthValue = 11
    if (month < minMonthValue) {
      throw WrongParamValueException("Month value can not be less than 0")
    }
    if (month > maxMonthValue) {
      throw WrongParamValueException("Month value can not be more than " + maxMonthValue + " for the selected month")
    }
    else {
      month
    }
  }

  /** Check year.
   *
   * @param year Year value to check.
   * @return Year value if it is greater than minimum year value and less than maximum year value.
   * @throws WrongParamValueException if year value is less than minimum year value or greater than maximum
   *                                          year value.
   */
  def checkYear(year: Int): Int = {
    val minYearValue = 0
    val maxYearValue = 2021
    if (year < minYearValue) {
      throw WrongParamValueException("Year value can not be less than 0")
    }
    if (year > maxYearValue) {
      throw WrongParamValueException("Year value can not be more than " + maxYearValue + " for the selected month")
    }
    else {
      year
    }
  }


  /** Check integervalue is not less than cero.
   *
   * @param value Integer value to check.
   * @throws WrongParamValueException if value to check is less than cero.
   */
  def checkNotNegativeInt(value: Int): Unit = {
    if (value < 0) {
      throw WrongParamValueException("Int param value can not be less than 0")
    }
  }

  /** Check long value is not less than cero.
   *
   * @param value Long Value to check.
   * @throws WrongParamValueException if value to check is less than cero.
   */
  def checkNotNegativeLong(value: Long): Unit = {
    if (value < 0) {
      throw WrongParamValueException("Long param value can not be less than 0")
    }
  }

  /** Check text is not empty.
   *
   * @param string Text to check.
   * @throws EmptyStringException if text is empty.
   */
  def checkNotEmptyString(string: String): Unit = {
    if (string.trim.isEmpty) {
      throw EmptyStringException("The string can not be empty or blank")
    }
  }

  /** Check not empty sequence.
   *
   * @param seq Sequence to check.
   * @throws IncorrectSizeListException if sequence is empty.
   */
  def checkNotEmptySeq(seq: Seq[Any]): Unit = {
    if (seq.isEmpty) {
      throw IncorrectSizeListException("List can not be empty")
    }
  }

  /** Check not empty linked list.
   *
   * @param list Linked list to check.
   * @throws IncorrectSizeListException if linked list is empty.
   */
  def checkNotEmptyLinkedList(list: java.util.LinkedList[Int]): Unit = {
    if (list.isEmpty) {
      throw IncorrectSizeListException("List can not be empty")
    }
  }

  /** Check exampleLength is less than file characters.
   *
   * @param exampleLength Example length.
   * @param fileCharacterToReturn File characters list.
   */
  def checkFileLength(exampleLength: Int, fileCharacterToReturn: util.ArrayList[Char]): Unit = {
    if (exampleLength >= fileCharacterToReturn.size) {
      throw IncorrectSizeListException("exampleLength=" + exampleLength + "cannot exceed number of valid " +
        "characters in file (" + fileCharacterToReturn.size + ")")
    }
  }

}
