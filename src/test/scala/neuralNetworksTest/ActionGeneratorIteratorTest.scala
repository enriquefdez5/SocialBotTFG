package neuralNetworksTest

import model.exceptions.IncorrectSizeListException
import neuralNetworks.rnnActionGenerator.ActionGeneratorIterator
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertFalse, assertNotNull, assertTrue}
import org.junit.jupiter.api.Test

class ActionGeneratorIteratorTest {

  private def getIterator(): ActionGeneratorIterator = {
    val arraySize = 18
    val trainingData: Array[String] = new Array[String](arraySize)
    for ( i <- 0 until 18) {
      if (i == 9) {
        trainingData(i) = "-1"
      }
      else {
        trainingData(i) = "1,1,1"
      }
    }

    val miniBatchSize = 6
    val exampleLength = 10
    new ActionGeneratorIterator(miniBatchSize, exampleLength, trainingData)
  }
  @Test
  def hasNextTest(): Unit = {

    val iterator = getIterator

    // there is next
    assertTrue(iterator.hasNext)

    // Only one separator, just 2 element on list
    iterator.next
    // last element does not have next
    assertFalse(iterator.hasNext)

  }

  @Test
  def nextTest(): Unit = {
    val iterator = getIterator

    // Only one separator, just 2 element on list
    assertNotNull(iterator.next)

    // last element does not have next
    val emptyListExceptionMessage = "List can not be empty"
    try {
      iterator.next
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }
  }

  @Test
  def inputColumnsTest(): Unit = {
    val iterator = getIterator
    assertEquals(3, iterator.inputColumns())
  }

  @Test
  def outputColumnsTest(): Unit = {
    val iterator = getIterator
    assertEquals(3, iterator.totalOutcomes())
  }


  @Test
  def resetTest(): Unit = {
    val iterator = getIterator()
    iterator.next
    iterator.reset
    assertNotNull(iterator.next)
  }

  @Test
  def batchTest(): Unit = {
    val iterator = getIterator()
    val minibatchSize = 6
    assertEquals(minibatchSize, iterator.batch())
  }
}
