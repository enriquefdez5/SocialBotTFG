package rnn

import java.util
import java.util.{Collections, Random}

import org.apache.logging.log4j.scala.Logging
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j

class CharacterIterator(miniBatchSize: Int, exampleLength: Int, validCharacters: String, rng: Random,
                        splittedData: String) extends DataSetIterator with Logging {

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
    var fileCharacterToReturn: Array[Char] = null

    val newLineValid: Boolean = charToIdxMap.containsKey('\n')
    val lines: Array[String] = splittedData.split("\n")

    var maxSize: Int = lines.length
    lines.foreach(s => maxSize += s.length)

    val characters: Array[Char] = new Array[Char](maxSize)
    var currentIndex: Int = 0
    lines.foreach(s => {
      val thisLine = s.toCharArray
      thisLine.foreach(character => {
        if (charToIdxMap.containsKey(character)) {
          currentIndex+=1
          characters(currentIndex) = character
        }
      })
      if (newLineValid) {
        currentIndex+=1
        characters(currentIndex) = '\n'
      }
    })
    if (currentIndex == characters.length) {
      fileCharacterToReturn = characters
    }
    else {
      fileCharacterToReturn = util.Arrays.copyOfRange(characters, 0, currentIndex)
    }
    if (exampleLength >= fileCharacterToReturn.length) {
      throw new IllegalArgumentException("exampleLength=" + exampleLength + "cannot exceed number of valid " +
        "characters in file (" + fileCharacterToReturn.length + ")")
    }
    val nRemoved = maxSize - fileCharacterToReturn.length
    logger.debug("Loaded and converted file: " + fileCharacterToReturn.length + " valid characters of " + maxSize + "" +
      " total characters (" + nRemoved + " removed")
    fileCharacterToReturn
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

    for(i <- 0 until currMiniBatchSize) {
      val startIdx: Int = exampleStartOffsets.removeFirst()
      val endIdx: Int = startIdx + exampleLength
      var currCharIdx: Int = charToIdxMap.get(fileCharacters(startIdx))    // Current input
      var c = 0
      for(j <- startIdx + 1 until endIdx) {
        val nextCharIdx = charToIdxMap.get(fileCharacters(j))
        input.putScalar(Array[Int](i, currCharIdx, c), 1.0)
        labels.putScalar(Array[Int](i, nextCharIdx, c), 1.0)
        currCharIdx = nextCharIdx
        c = c + 1
      }
    }
    new DataSet(input, labels)
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
