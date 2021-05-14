package neuralNetworks.rnnCharacterGenerator

import java.util
import java.util.{Collections, Random}
import scala.annotation.tailrec

import org.apache.logging.log4j.scala.Logging
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j

import utilities.console.ConsoleUtilTrait
import utilities.validations.ValidationsUtilTrait


/** Class to create over character generation training data and create datasets.
 *
 * @constructor Create a new character generator iterator with the given data, minibatch size and example length.
 * @param miniBatchSize Size of the minibatch.
 * @param exampleLength Length of each example created.
 * @param data Data for training.
 */
class CharacterGeneratorIterator(miniBatchSize: Int, exampleLength: Int, data: Array[String]) extends DataSetIterator
                                                                                            with Logging
                                                                                            with ValidationsUtilTrait
                                                                                            with ConsoleUtilTrait {

   val validCharacters = "abcdefghijklmnñopqrstuvwxyzáéíóú1234567890\n,.#@ "

  val exampleStartOffsets: util.LinkedList[Int] = new util.LinkedList[Int]

  val charToIdxMap: util.Map[Char, Int] = initializeCharToMap()

  val fileCharacters: Array[Char] = setFileCharacters(data)
  initializeOffsets()

  val notImplementedString: String = "Not implemented yet"

  private def initializeCharToMap(): util.HashMap[Char, Int] = {
    val charToIdx = new util.HashMap[Char, Int]()
    for (i <- 0 until validCharacters.length) {
      charToIdx.put(validCharacters(i), i)
    }
    charToIdx
  }


  private def setFileCharacters(lines: Array[String]): Array[Char] = {
    val linesSize: Array[String] = lines.clone()
    val maxSize = linesSize.map(s => s.length).sum

    val fileCharacterToReturn: util.ArrayList[Char] = new util.ArrayList[Char]()
    val currentIndex: Int = 0
    readLines(fileCharacterToReturn, lines, currentIndex)
    checkFileLength(exampleLength, fileCharacterToReturn)
    showFileInfo(maxSize, fileCharacterToReturn)

    val idx = 0
    val arrayToReturn: Array[Char] = new Array[Char](fileCharacterToReturn.size())
    transformIntoArray(fileCharacterToReturn, arrayToReturn, idx)
    arrayToReturn
  }


  @tailrec
  private def transformIntoArray(fileCharacterToReturn: util.ArrayList[Char], arrayToReturn: Array[Char], idx: Int)
  : Unit = {
    if (idx < fileCharacterToReturn.size()) {
      arrayToReturn(idx) = fileCharacterToReturn.get(idx)
      transformIntoArray(fileCharacterToReturn, arrayToReturn, idx + 1)
    }
  }

  @tailrec
  private def readLines(fileCharacterToReturn: util.ArrayList[Char], lines: Array[String], idx: Int): Unit = {
    if (idx < lines.length) {
      val thisLine = lines(idx).toCharArray
      thisLine.foreach(character => {
        if (charToIdxMap.containsKey(character)) {
          fileCharacterToReturn.add(character)
        }
      })
      if (charToIdxMap.containsKey('\n')) {
        fileCharacterToReturn.add('\n')
      }
      readLines(fileCharacterToReturn, lines, idx + 1)
    }
  }


  /** Get next dataset.
   *
   * @param num. Size of dataset.
   * @return Dataset
   */
  override def next(num: Int): DataSet = {
    checkNotNegativeInt(num)
    checkNotEmptyLinkedList(exampleStartOffsets)
    val currMiniBatchSize = Math.min(num, exampleStartOffsets.size())

    val input: INDArray = Nd4j.create(Array[Int](currMiniBatchSize, validCharacters.length, exampleLength), 'f')
    val labels: INDArray = Nd4j.create(Array[Int](currMiniBatchSize, validCharacters.length, exampleLength ), 'f')

    val idx = 0
    createDataSet(input, labels, currMiniBatchSize, idx)
    new DataSet(input, labels)
  }

  @tailrec
  private def createDataSet(input: INDArray, labels: INDArray, currMiniBatchSize: Int, idx: Int): Unit = {
    if (idx < currMiniBatchSize) {
      val startIdx: Int = exampleStartOffsets.removeFirst()
      val endIdx: Int = startIdx + exampleLength

      val currCharIdx: Int = charToIdxMap.get(fileCharacters(startIdx))    // Current input
      val c = 0
      val secondIdx = startIdx + 1
      fillArrays(input, labels, currCharIdx, idx, secondIdx, endIdx, c)
      createDataSet(input, labels, currMiniBatchSize, idx + 1)
    }
  }

  @tailrec
  private def fillArrays(input: INDArray, labels: INDArray, currCharIdx: Int,
                         idx: Int, secondIdx: Int, endIdx: Int, c: Int): Unit = {
    if (secondIdx < endIdx) {
      val nextCharIdx = charToIdxMap.get(fileCharacters(secondIdx))
      input.putScalar(Array[Int](idx, currCharIdx, c), 1.0)
      labels.putScalar(Array[Int](idx, nextCharIdx, c), 1.0)
      fillArrays(input, labels, nextCharIdx, idx, secondIdx + 1, endIdx, c + 1)
    }
  }

  /**
   * @return input columns size.
   */
  override def inputColumns(): Int = validCharacters.length

  /**
   * @return output columns size.
   */
  override def totalOutcomes(): Int = validCharacters.length

  /**
   * @return true because iterator can be reset.
   */
  override def resetSupported(): Boolean = true

  /**
   * @return true because iterator supports async.
   */
  override def asyncSupported(): Boolean = true

  /** Resets the iterator */
  override def reset(): Unit = {
    exampleStartOffsets.clear()
    initializeOffsets()
  }

  private def initializeOffsets(): Unit = {
    val nMiniBatchesPerEpoch = (fileCharacters.length - 1) / exampleLength - 2 // -2: for end index, and for partial
    for(i <- 0 until nMiniBatchesPerEpoch) {
      exampleStartOffsets.add(i*exampleLength)
    }
    val rng = new Random()
    Collections.shuffle(exampleStartOffsets, rng)
  }

  /**
   * @return Batch size.
   */
  override def batch(): Int = miniBatchSize

  /**
   * @throws UnsupportedOperationException because it is not implemented.
   * @param dataSetPreProcessor Dataset preprocessor object.
   */
  override def setPreProcessor(dataSetPreProcessor: DataSetPreProcessor): Unit =
    throw new UnsupportedOperationException(notImplementedString)

  /**
   * @throws UnsupportedOperationException because it is not implemented.
   * @return Dataset preprocessor object.
   */
  override def getPreProcessor: DataSetPreProcessor =
    throw new UnsupportedOperationException(notImplementedString)

  /**
   * @throws UnsupportedOperationException because it is not implemented.
   * @return Dataset preprocessor object.
   */
  override def getLabels: util.List[String] =
    throw new UnsupportedOperationException(notImplementedString)

  /**
   * @return True if there is more datasets available for the current training data.
   */
  override def hasNext: Boolean = exampleStartOffsets.size() > 0

  /**
   * @return The next dataset.
   */
  override def next(): DataSet = next(miniBatchSize)

  /** Transform a character into an index from the dictionary map.
   *
   * @param c Character to convert into index integer value.
   * @return Index in dictionary map.
   */
  def convertCharacterToIndex(c: Char ): Int = {
    charToIdxMap.get(c)
  }

  /** Transfor an integer from the character dictionary map object into a character.
   *
   * @param idx Index to convert into character.
   * @return Character.
   */
  def convertIndexToChar(idx: Int): Char = {
      validCharacters(idx)
  }
}
