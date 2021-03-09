package test.scala.utilitiesTest.validationsTest

import java.util

import model.Action.{Action, getActionFromIntValue}
import model.TypeAndDate.{maxActionValue, minActionValue}
import model.exceptions.{EmptyStringException, IncorrectSizeListException, WrongParamValueException}
import org.junit.Assert
import org.junit.jupiter.api.Test
import utilities.validations.ValidationsUtil

object ValidationsUtilTest extends ValidationsUtil {

  @Test
  def checkActionValueTest(): Unit = {
    // Min action value
    val action1: Action = getActionFromIntValue(minActionValue)
    checkActionValue(action1)
    // Max action value
    val action2: Action = getActionFromIntValue(maxActionValue)
    checkActionValue(action2)
  }

  @Test
  def checkValueTest(): Unit = {
    val minValue = 1
    val maxValue = 10

    // Value is equal to min value
    checkValue(minValue, minValue, maxValue)
    // Value is less than min
    val exceptionMsg: String = "Param value is not valid"

    try {
      checkValue(minValue - 1, minValue, maxValue)
    }
    catch {
      case exception: WrongParamValueException => Assert.assertEquals(exceptionMsg, exception.msg)
    }
    // Value is equal to max and min is not specified
    checkValue(maxValue, max = maxValue)
    // Value is greater than max and min is not specified
    try {
      checkValue(maxValue + 1, max = maxValue)
    }
    catch {
      case exception: WrongParamValueException => Assert.assertEquals(exceptionMsg, exception.msg)
    }
    // Value is okey
    checkValue(minValue + 5, minValue, maxValue)
  }

  @Test
  def checkNotNegativeIntTest(): Unit = {
    // Positive value
    checkNotNegativeInt(0)
    // Negative value
    val exceptionMsg = "Int param value can not be less than 0"
    try {
      checkNotNegativeInt(0)
    }
    catch {
      case exception: WrongParamValueException => Assert.assertEquals(exceptionMsg, exception.msg)
    }
  }

  @Test
  def checkNotNegativeLongTest(): Unit = {
    // Positive limit value
    checkNotNegativeLong(0.01.toLong)
    // Positive value
    checkNotNegativeLong(100.toLong)


    // Exception msg
    val exceptionMsg = "Long param value can not be less than 0"

    // Negative limit value
    try {
      checkNotNegativeLong(-0.01.toLong)
    }
    catch {
      case exception: WrongParamValueException => Assert.assertEquals(exceptionMsg, exception.msg)
    }
    // Negative value
    try {
      checkNotNegativeLong(-100.toLong)
    }
    catch {
      case exception: WrongParamValueException => Assert.assertEquals(exceptionMsg, exception.msg)
    }
  }

  @Test
  def checkNotEmptyStringTest(): Unit = {
    // Exception msg
    val exceptionMsg: String = "The string can not be empty or blank"

    // Empty String
    val emptyString = ""
    try {
      checkNotEmptyString(emptyString)
    }
    catch {
      case exception: EmptyStringException => Assert.assertEquals(exceptionMsg, exception.msg)
    }

    // White space string
    val whiteSpaceString = " "
    try {
      checkNotEmptyString(whiteSpaceString)
    }
    catch {
      case exception: EmptyStringException => Assert.assertEquals(exceptionMsg, exception.msg)
    }

    // not empty string
    val notEmptyString = "notEmptyString"
    checkNotEmptyString(notEmptyString)
  }

  @Test
  def checkNotEmptyListTest(): Unit = {
    // Exception msg
    val exceptionMsg: String = "List can not be empty"

    // Check empty list
    try {
      checkNotEmptySeq(Seq())
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(exceptionMsg, exception.msg)
    }

    // Check not empty list
    checkNotEmptySeq(Seq[Int](1))
  }

  @Test
  def checkNotEmptyLinkedListTest(): Unit = {
    val exceptionMsg: String = "List can not be empty"

    // Empty linked list
    try {
      checkNotEmptyLinkedList(new util.LinkedList[Int]())
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(exceptionMsg, exception.msg)
    }

    // Not empty linked list
    val linkedList: util.LinkedList[Int] = new util.LinkedList[Int]()
    linkedList.add(1)
    checkNotEmptyLinkedList(linkedList)
  }

}
