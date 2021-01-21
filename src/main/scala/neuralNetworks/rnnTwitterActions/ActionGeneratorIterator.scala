package neuralNetworks.rnnTwitterActions

import java.util
import java.util.{Collections, Random}

import org.apache.logging.log4j.scala.Logging
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j

import scala.annotation.tailrec

class ActionGeneratorIterator(miniBatchSize: Int, exampleLength: Int,
                              strings: Array[String])
                                    extends DataSetIterator with Logging {

  // Get values from strings array parameter
  val values: util.List[String] = getValues(0, new util.ArrayList[String]())

  // Get offsets and sizes from the computed values variable
  val offsetsAndSize: util.TreeMap[Int, Int] = new util.TreeMap[Int, Int]()
  initializeOffsets(0, 0, new util.TreeMap[Int, Int]())

  // Get keys for every split from values variable
  val keys: util.LinkedList[Int] = getKeys

  // Not implemented error msg
  val notImplementedError = "Not implemented yet"

  /**
   * Private function for initializing values variable with the content from the given list of tweets.
   */
  @tailrec
  private def getValues(idx: Int, values: util.ArrayList[String]): util.List[String] = {
    if (idx < strings.length) {
      if (strings(idx).length > 2) {
        values.add(strings(idx))
      }
      getValues(idx + 1, values)
    }
    else {
      values
    }
  }

  /**
   * Private function that initialize the offsets of the iterator. It also shuffles the keys linked list.
   */
  @tailrec
  private def initializeOffsets(idx: Int, count: Int, map: util.TreeMap[Int, Int]): util.TreeMap[Int, Int] = {
    //    var count = -1
    //    for(i <- 0 until values.size()) {
    //      count+=1
    //      if (strings(i) == "-1" ){
    //        if (i != 0) {
    //          offsetsAndSize.put(i-count, count)
    //          count = 0
    //        }
    //        else {
    //          count = 0
    //        }
    //      }
    //    }
    //TODO Check if this works. The call below is the same as the commented code above.
    if (idx < values.size()) {
      if (strings(idx) == "-1") {
        if (idx != 0) {
          map.put(idx-count, count)
          initializeOffsets(idx + 1, 1, map)
        }
        else { initializeOffsets(idx + 1, 1, map) }
      }
      else { initializeOffsets(idx + 1, count + 1, map)}
    }
    else { map }
    // TODO Check if this works. The commented code below is the same as getKeys() function.
    // val keysToShuffle = new util.LinkedList[Int](offsetsAndSize.keySet())
    // val seed = 12345
    // Collections.shuffle(keysToShuffle, new Random(seed))
    // keys = keysToShuffle
  }

  /**
   * Private function to get and shuffle keys of offsetsAndSize
   * @return
   */
  private def getKeys: util.LinkedList[Int] = {
    val keysToShuffle = new util.LinkedList[Int](offsetsAndSize.keySet())
    val seed = 12345
    Collections.shuffle(keysToShuffle, new Random(seed))
    keysToShuffle
  }

  /**
   * Override function for getting next dataset with the given minibatch size.
   * @param num, Int. Size of the next dataset.
   * @return Dataset. Dataset built with the given size.
   */
  override def next(num: Int): DataSet = {
    if (keys.size() == 0) throw new NoSuchElementException()
    val currMiniBatchSize = Math.min(num, keys.size())

    val input: INDArray = Nd4j.create(Array[Long](currMiniBatchSize, 3, exampleLength), 'f')
    val labels: INDArray = Nd4j.create(Array[Long](currMiniBatchSize, 3, exampleLength), 'f')

    val idx = 0

    createDataSet(input, labels, currMiniBatchSize, idx)
    new DataSet(input, labels)
  }

  /**
   * Private tailrec function for creating a dataset.
   * @param input, INDArray. INDArray to fill with input values.
   * @param labels, INDArray. INDArray to fill with labels values.
   * @param currMiniBatchSize, Int. Mini batch size for input and labels size.
   * @param idx, Int. Index value for stopping the tailrec loop.
   */
  @tailrec
  private def createDataSet(input: INDArray, labels: INDArray, currMiniBatchSize: Int, idx: Int): Unit = {
    if (idx < currMiniBatchSize) {
      val startIdx = keys.removeFirst()
      val currMiniBatchNumberOfElements = offsetsAndSize.get(startIdx)

      val endIdx = startIdx + currMiniBatchNumberOfElements

      val firstLine = values.get(startIdx).split(",")
      val secondIdx = startIdx + 1
      val c = 0
      fillArrays(input, labels, firstLine, idx, secondIdx, endIdx, c)
      createDataSet(input, labels, currMiniBatchSize, idx + 1)
    }
  }

  /**
   * Private function that fills in the input and labels array with the characters content.
   * @param input, INDArray. INDArray to fill with input values.
   * @param labels, INDArray. INDArray to fill with labels values.
   * @param line, Array[String]. Array of strings that contains the content to be added to the INDArray..
   * @param idx, Int. Index value to specify in which position the line content will be added.
   * @param secondIdx, Int. Another index to specify the next line.
   * @param endIdx, Int. Last index that represents the end of the tailrec loop.
   * @param c, Int. Value for building the INDArrays.
   */
  @tailrec
  private def fillArrays(input: INDArray, labels: INDArray, line: Array[String],
                         idx: Int, secondIdx: Int, endIdx: Int, c: Int): Unit = {
    if (secondIdx < endIdx) {
      val nextLine: Array[String] = values.get(secondIdx).split(",")
      input.putScalar(Array[Long](idx, 0, c), line(0).toInt/7.0)
      input.putScalar(Array[Long](idx, 1, c), line(1).toInt/23.0)
      input.putScalar(Array[Long](idx, 2, c), line(2).toInt/3.0)
      labels.putScalar(Array[Long](idx, 0, c), nextLine(0).toInt/7.0)
      labels.putScalar(Array[Long](idx, 1, c), nextLine(1).toInt/23.0)
      labels.putScalar(Array[Long](idx, 2, c), nextLine(2).toInt/3.0)
      fillArrays(input, labels, nextLine, idx, secondIdx + 1, endIdx, c + 1)
    }
  }

  /**
   * Override function that gives the number of input columns of the iterator.
   * @return Int. The number of input columns.
   */
  override def inputColumns(): Int = 3

  /**
   * Override function that gives the total outcomes of the iterator.
   * @return Int. The number of total outcomes.
   */
  override def totalOutcomes(): Int = 3

  /**
   * Override function that returns if reset is supported.
   * @return Boolean. True because reset is supported.
   */
  override def resetSupported(): Boolean = true

  /**
   * Override function that returns if async is supported.
   * @return Boolean. True because async is supported.
   */
  override def asyncSupported(): Boolean = true

  /**
   * Override function that resets the iterator. It clears the keys map and calls the initializeOffsets function.
   */
  override def reset(): Unit = {
    keys.clear()
    initializeOffsets(0, 0, new util.TreeMap[Int, Int]())
  }

  /**
   * Override function that gets the batch size of the iterator.
   * @return Int. Mini batch size.
   */
  override def batch(): Int = miniBatchSize

  /**
   * Override function that returns if there is a next dataset available.
   * @return Boolean. If there is more datasets available.
   */
  override def hasNext: Boolean = keys.size() > 0

  /**
   * Override function that calls the next function asking for the next dataset.
   * @return Dataset. The next dataset available.
   */
  override def next(): DataSet = next(miniBatchSize)

  /**
   * Override function that throws an exception because its an unsupported operation.
   * @param dataSetPreProcessor, DataSetPreProcessor.
   */
  override def setPreProcessor(dataSetPreProcessor: DataSetPreProcessor): Unit =
    throw new UnsupportedOperationException(notImplementedError)

  /**
   * Override function that throws an exception because its an unsupported operation.
   * @return DataSetPreProcessor DataSetPreProcessor.
   */
  override def getPreProcessor: DataSetPreProcessor =
    throw new UnsupportedOperationException(notImplementedError)

  /**
   * Override function that throws an exception because its an unsupported operation.
   * @return List[String]
   */
  override def getLabels: util.List[String] =
    throw new UnsupportedOperationException(notImplementedError)




}
