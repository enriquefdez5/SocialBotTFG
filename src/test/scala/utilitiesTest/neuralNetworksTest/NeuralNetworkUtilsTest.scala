package test.scala.utilitiesTest.neuralNetworksTest

import java.util.Date

import model.exceptions.{IncorrectSizeListException, NotExistingFileException, WrongParamValueException}
import model.{Post, TypeAndDate}
import org.junit.Assert
import org.junit.jupiter.api.Test
import utilities.neuralNetworks.NeuralNetworkUtils

object NeuralNetworkUtilsTest extends NeuralNetworkUtils {

  val negativeLongExceptionMessage = "Long param value can not be less than 0"
  val negativeIntExceptionMessage = "Int param value can not be less than 0"
  val emptyListExceptionMessage = "List can not be empty"


  @Test
  def prepareTextTest(): Unit = {
    // Negative number of characters
    val negativeNumberOfChars = -10
    try {
      prepareText(negativeNumberOfChars)
    }
    catch {
      case exception: WrongParamValueException => Assert.assertEquals(negativeIntExceptionMessage, exception.msg)
    }

    // Postivive number of characters
    val positiveNumberOfChars = 100
    val initialChar = 1
    Assert.assertEquals(positiveNumberOfChars + initialChar, prepareText(positiveNumberOfChars).length )
  }

  @Test
  def loadNetworkTest(): Unit = {
    // existing route
    loadNetwork(getProperties.getProperty("textNNPath"))
    // non existing route
    val route = "./thisIsNotARoute/nonexistingRoute.zip"
    val exceptionMsg = "File " + route + " does not exist"
    try {
      loadNetwork(route)
    }
    catch {
      case exception: NotExistingFileException => Assert.assertEquals(exceptionMsg, exception.msg)
    }
  }

  @Test
  def generateNextActionTest(): Unit = {
    val wrongFollowedPostActions = -5
    val goodFollowedPostActions = 3
    val wrongMaxFollowedPostActions = -5
    val goodMaxFollowedPostActions = 7
    val goodTweetsSeq = Seq[Post](
      Post("this is a Post", new Date(), null, 0, 0),
      Post("this is a Post 2", new Date(), null, 0, 0),
      Post("this is a Post 3", new Date(), null, 0, 0),
      Post("this is a Post 4", new Date(), null, 0, 0),
      Post("this is a Post 5", new Date(), null, 0, 0)
    )
    val wrongTweetsSeq = Seq[Post]()


    // Wrong param 1
    try {
      generateNextAction(wrongFollowedPostActions, goodMaxFollowedPostActions, goodTweetsSeq)
    }
    catch {
      case exception: WrongParamValueException => Assert.assertEquals(negativeIntExceptionMessage, exception.msg)
    }

    // Wrong param 2
    try {
      generateNextAction(goodFollowedPostActions, wrongMaxFollowedPostActions, goodTweetsSeq)
    }
    catch {
      case exception: WrongParamValueException => Assert.assertEquals(negativeIntExceptionMessage, exception.msg)
    }

    // Wrong param 3
    try {
      generateNextAction(goodFollowedPostActions, goodMaxFollowedPostActions, wrongTweetsSeq)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // Good call
    val action: TypeAndDate = generateNextAction(goodFollowedPostActions, goodMaxFollowedPostActions,
      goodTweetsSeq)
    Assert.assertNotNull(action)
  }

  @Test
  def getOutputDayTest(): Unit = {
    // Double input is negative
    val wrongInputDay = -5
    try {
      getOutputDay(wrongInputDay)
    }
    catch {
      case exception: WrongParamValueException => Assert.assertEquals(negativeLongExceptionMessage, exception.msg)
    }
    // Double input is max value == 1
    val maxOutputPossible = 1
    Assert.assertEquals(maxOutputPossible*7, getOutputDay(maxOutputPossible))

    // Double input is min value == 0
    val minOutputPossible = 0
    Assert.assertEquals(minOutputPossible, getOutputDay(minOutputPossible))
  }


  @Test
  def getOutputHourTest(): Unit = {
    // Double input is negative
    val wrongInputHour = -5
    try {
      getOutputHour(wrongInputHour)
    }
    catch {
      case exception: WrongParamValueException => Assert.assertEquals(negativeLongExceptionMessage, exception.msg)
    }
    // Double input is max value == 1
    val maxOutputPossible = 1
    Assert.assertEquals(maxOutputPossible*23, getOutputHour(maxOutputPossible))

    // Double input is min value == 0
    val minOutputPossible = 0
    Assert.assertEquals(minOutputPossible, getOutputHour(minOutputPossible))
  }

  @Test
  def getOutputActionTest(): Unit = {
    // Double input is negative
    val wrongInputAction = -5
    try {
      getOutputAction(wrongInputAction)
    }
    catch {
      case exception: WrongParamValueException => Assert.assertEquals(negativeLongExceptionMessage, exception.msg)
    }
    // Double input is max value == 1
    val maxOutputPossible = 1
    Assert.assertEquals(maxOutputPossible*3, getOutputAction(maxOutputPossible))

    // Double input is min value == 0
    val minOutputPossible = 0
    Assert.assertEquals(minOutputPossible, getOutputAction(minOutputPossible))
  }


}

