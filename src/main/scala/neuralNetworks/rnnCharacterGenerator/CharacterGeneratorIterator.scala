package neuralNetworks.rnnCharacterGenerator

import java.util
import java.util.{Collections, Random}

import org.apache.logging.log4j.scala.Logging
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j

import scala.annotation.tailrec

class CharacterGeneratorIterator(miniBatchSize: Int, exampleLength: Int, rng: Random,
                                 splittedData: String) extends DataSetIterator with Logging {

  // Valid chars used for training
  //    val validCharacters = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZabcdefghijklmnñopqrstuvwxyzáéíóú1234567890\"\n',.?;()[]{}:!-#@ "
  //    val validCharacters = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZabcdefghijklmnñopqrstuvwxyzáéíóú1234567890\n,.#@ "
  val validCharacters = "abcdefghijklmnñopqrstuvwxyzáéíóú1234567890\n,.#@ "

  // Offsets for the start of each example
  val exampleStartOffsets: util.LinkedList[Int] = new util.LinkedList[Int]

  // Store valid characters for vectorization
  val charToIdxMap: util.Map[Char, Int] = initializeCharToMap()

  // All characters of the input file (after filtering to only those that are valid)
  val fileCharacters: Array[Char] = setFileCharacters()
  initializeOffsets()

  val notImplementedString: String = "Not implemented yet"

  def initializeCharToMap(): util.HashMap[Char, Int] = {
    val charToIdx = new util.HashMap[Char, Int]()
    for (i <- 0 until validCharacters.length) {
      charToIdx.put(validCharacters(i), i)
    }
    charToIdx
  }


  def setFileCharacters(): Array[Char] = {
    // Split file into lines
    val lines: Array[String] = splittedData.split("\n")
    // Get file max size
    val linesSize: Array[String] = lines.clone()
    val maxSize = linesSize.map(s => s.length).sum

    // Get characters from lines and show info of character readed
    val fileCharacterToReturn: util.ArrayList[Char] = new util.ArrayList[Char]()
    val currentIndex: Int = 0
    readLines(fileCharacterToReturn, lines, currentIndex)
    checkLength(fileCharacterToReturn)
    showInfo(maxSize, fileCharacterToReturn)

    // Transform into array
    val idx = 0
    val arrayToReturn: Array[Char] = new Array[Char](fileCharacterToReturn.size())
    transformIntoArray(fileCharacterToReturn, arrayToReturn, idx)
    arrayToReturn
  }

  private def showInfo(maxSize: Int, fileCharacterToReturn: util.ArrayList[Char]): Unit = {
    val fileCharacterSize = fileCharacterToReturn.size
    val nRemoved = maxSize - fileCharacterSize
    logger.debug("Loaded and converted file: " + fileCharacterSize + " valid characters of " + maxSize + "" +
      " total characters (" + nRemoved + " removed")
    logger.debug("Number of characters in file: " + fileCharacterSize)
  }

  private def checkLength(fileCharacterToReturn: util.ArrayList[Char]): Unit = {
    if (exampleLength >= fileCharacterToReturn.size) {
      throw new IllegalArgumentException("exampleLength=" + exampleLength + "cannot exceed number of valid " +
        "characters in file (" + fileCharacterToReturn.size + ")")
    }
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


  override def next(num: Int): DataSet = {
    if (exampleStartOffsets.size() == 0 ) throw new NoSuchElementException()

    val currMiniBatchSize = Math.min(num, exampleStartOffsets.size())
    // Allocate space:
    // Note the order here:
    // dimension 0 = number of examples in minibatch
    // dimension 1 = size of each vector (i.e., number of characters)
    // dimension 2 = length of each time series/example

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

  override def inputColumns(): Int = validCharacters.length

  override def totalOutcomes(): Int = validCharacters.length

  override def resetSupported(): Boolean = true

  override def asyncSupported(): Boolean = true

  override def reset(): Unit = {
    exampleStartOffsets.clear()
    initializeOffsets()
  }
  private def initializeOffsets(): Unit = {
    // This defines the order in which parts of the file are fetched
    val nMiniBatchesPerEpoch = (fileCharacters.length - 1) / exampleLength - 2 // -2: for end index, and for partial
    for(i <- 0 until nMiniBatchesPerEpoch) {
      exampleStartOffsets.add(i*exampleLength)
    }
    Collections.shuffle(exampleStartOffsets, rng)
  }

  override def batch(): Int = miniBatchSize

  override def setPreProcessor(dataSetPreProcessor: DataSetPreProcessor): Unit =
    throw new UnsupportedOperationException(notImplementedString)

  override def getPreProcessor: DataSetPreProcessor =
    throw new UnsupportedOperationException(notImplementedString)

  override def getLabels: util.List[String] =
    throw new UnsupportedOperationException(notImplementedString)

  override def hasNext: Boolean = exampleStartOffsets.size() > 0

  override def next(): DataSet = next(miniBatchSize)

  def convertCharacterToIndex(c: Char ): Int = {
    charToIdxMap.get(c)
  }

  def convertIndexToChar(idx: Int): Char = {
      validCharacters(idx)
  }
}
