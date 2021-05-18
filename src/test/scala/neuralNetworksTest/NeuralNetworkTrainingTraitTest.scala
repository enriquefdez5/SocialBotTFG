package neuralNetworksTest

import java.io.FileNotFoundException
import java.util.Date

import model.exceptions.{IncorrectSizeListException, NotExistingFileException, WrongParamValueException}
import model.{NNActionItem, StatusImpl}
import neuralNetworks.NeuralNetworkTrainingTrait
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertThrows, assertTrue}
import org.junit.jupiter.api.Test

class NeuralNetworkTrainingTraitTest extends NeuralNetworkTrainingTrait {

  val negativeLongExceptionMessage = "Long param value can not be less than 0"
  val negativeIntExceptionMessage = "Int param value can not be less than 0"
  val emptyListExceptionMessage = "List can not be empty"
  val fileNotFoundMessage = "File not found"
  val fileNotFoundGetDataMessage = ".\\data(generated)\\asdadnaskjda.txt"


  @Test
  def getDataTest(): Unit = {
    // File does not exist
    try {
      val wrongUsername = "asdadnaskjda"
      getData(wrongUsername, true)
    }
    catch {
      case exception: FileNotFoundException =>
        assertTrue(exception.getMessage.contains(fileNotFoundGetDataMessage))
    }
    // Get txt file
    val testFile: String = "testFile"
    assertTrue(getData(testFile, true).size > 0)

    // Get csv file
    assertTrue(getData(testFile, false).size > 0)
  }

  @Test
  def getTotalPercentageTest(): Unit = {
    assertEquals(totalPercentage, getTotalPercentage)
  }

  @Test
  def getTrainingPercentageTest(): Unit = {
    assertEquals(trainingPercentage, getTrainingPercentage)
  }

  @Test
  def getTestPercentageTest(): Unit = {
    assertEquals(testPercentage, getTestPercentage)
  }

  @Test
  def getSplitSymbolTest(): Unit = {
    assertEquals(splitSymbol, getSplitSymbol)
  }

  @Test
  def createPathAndSaveNetworkTest(): Unit = {
    val network = loadNetwork("./models/testModelAction.zip")
    try {
      createPathAndSaveNetwork(network, "/Noexistelaruta/notARealPath", "Action")
    }
    catch {
      case exception: FileNotFoundException =>
        val msg = ".\\models\\Noexistelaruta\\notARealPathAction.zip"
        assertTrue(exception.getMessage.contains(msg))
    }

    val goodPath = "./goodPath"
    createPathAndSaveNetwork(network, goodPath, "Text")
    assertNotNull(loadNetwork(goodPath))
  }

  @Test
  def getTrainingDataTest(): Unit = {
    // empty list
    val firstIndex = 0
    val splitSize = 70
    try {
      getTrainingData(new Array[String](firstIndex), splitSize)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // data split
      // Positive split size
    val allData: Array[String] = new Array[String](3)
    allData(0) = "Primer valor"
    allData(1) = "Segundo valor"
    allData(2) = "Tercer valor"
    val trainingData = getTrainingData(allData, 1)
    assertEquals(1, trainingData.size)

      // Negative split value
    try {
      getTrainingData(allData, -1)
    }
    catch {
      case exception: WrongParamValueException =>
        assertEquals(negativeIntExceptionMessage, exception.msg)
    }
  }

  @Test
  def getTestDataTest(): Unit = {
    // empty list
    try {
      getTestData(new Array[String](0), 70)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // data split
    // Positive split size
    val allData: Array[String] = new Array[String](3)
    allData(0) = "Primer valor"
    allData(1) = "Segundo valor"
    allData(2) = "Tercer valor"
    val testData = getTestData(allData, 1)
    assertEquals(2, testData.size)

    // Negative split value
    try {
      getTestData(allData, -1)
    }
    catch {
      case exception: WrongParamValueException =>
        assertEquals(negativeIntExceptionMessage, exception.msg)
    }
  }

  @Test
  def prepareTextTest(): Unit = {
    // Negative number of characters
    val negativeNumberOfChars = -10
    try {
      prepareText("fede", negativeNumberOfChars)
    }
    catch {
      case exception: WrongParamValueException => assertEquals(negativeIntExceptionMessage, exception.msg)
    }

    // Postivive number of characters
    val positiveNumberOfChars = 100
    val initialChar = 1
    assertEquals(positiveNumberOfChars + initialChar, prepareText("testModel", positiveNumberOfChars).length )
  }

  @Test
  def loadNetworkTest(): Unit = {
    // existing route
    loadNetwork("./models/testModelAction.zip")
    // non existing route
    val route = "./thisIsNotARoute/nonexistingRoute.zip"
    val exceptionMsg = "File " + route + " does not exist"
    try {
      loadNetwork(route)
    }
    catch {
      case exception: NotExistingFileException => assertEquals(exceptionMsg, exception.msg)
    }
  }

  @Test
  def generateNextActionTest(): Unit = {
    val username = "testModel"
    val wrongFollowedPostActions = -5
    val goodFollowedPostActions = 3
    val wrongMaxFollowedPostActions = -5
    val goodMaxFollowedPostActions = 7
    val goodTweetsSeq = Seq[StatusImpl](
      StatusImpl("this is a Post", new Date(), 0, 0, null),
      StatusImpl("this is a Post 2", new Date(), 0, 0, null),
      StatusImpl("this is a Post 3", new Date(), 0, 0, null),
      StatusImpl("this is a Post 4", new Date(), 0, 0, null),
      StatusImpl("this is a Post 5", new Date(), 0, 0, null)
    )
    val wrongTweetsSeq = Seq[StatusImpl]()


    // Wrong param 1
    try {
      generateNextAction(username, wrongFollowedPostActions, goodMaxFollowedPostActions, goodTweetsSeq)
    }
    catch {
      case exception: WrongParamValueException => assertEquals(negativeIntExceptionMessage, exception.msg)
    }

    // Wrong param 2
    try {
      generateNextAction(username, goodFollowedPostActions, wrongMaxFollowedPostActions, goodTweetsSeq)
    }
    catch {
      case exception: WrongParamValueException => assertEquals(negativeIntExceptionMessage, exception.msg)
    }

    // Wrong param 3
    try {
      generateNextAction(username, goodFollowedPostActions, goodMaxFollowedPostActions, wrongTweetsSeq)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // Good call
    val action: NNActionItem = generateNextAction(username, goodFollowedPostActions, goodMaxFollowedPostActions,
      goodTweetsSeq)
    assertNotNull(action)
  }

  @Test
  def getOutputDayTest(): Unit = {
    // Double input is negative
    val wrongInputDay = -5
    try {
      getOutputDay(wrongInputDay)
    }
    catch {
      case exception: WrongParamValueException => assertEquals(negativeLongExceptionMessage, exception.msg)
    }
    // Double input is max value == 1
    val maxOutputPossible = 1
    assertEquals(maxOutputPossible*7, getOutputDay(maxOutputPossible))

    // Double input is min value == 0
    val minOutputPossible = 0
    assertEquals(minOutputPossible, getOutputDay(minOutputPossible))
  }


  @Test
  def getOutputHourTest(): Unit = {
    // Double input is negative
    val wrongInputHour = -5
    try {
      getOutputHour(wrongInputHour)
    }
    catch {
      case exception: WrongParamValueException => assertEquals(negativeLongExceptionMessage, exception.msg)
    }
    // Double input is max value == 1
    val maxOutputPossible = 1
    assertEquals(maxOutputPossible*23, getOutputHour(maxOutputPossible))

    // Double input is min value == 0
    val minOutputPossible = 0
    assertEquals(minOutputPossible, getOutputHour(minOutputPossible))
  }

  @Test
  def getOutputActionTest(): Unit = {
    // Double input is negative
    val wrongInputAction = -5
    try {
      getOutputAction(wrongInputAction)
    }
    catch {
      case exception: WrongParamValueException => assertEquals(negativeLongExceptionMessage, exception.msg)
    }
    // Double input is max value == 1
    val maxOutputPossible = 1
    assertEquals(maxOutputPossible*3, getOutputAction(maxOutputPossible))

    // Double input is min value == 0
    val minOutputPossible = 0
    assertEquals(minOutputPossible, getOutputAction(minOutputPossible))
  }


}

