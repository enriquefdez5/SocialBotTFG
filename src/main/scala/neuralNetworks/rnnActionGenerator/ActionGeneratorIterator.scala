package neuralNetworks.rnnActionGenerator

import java.util
import java.util.{Collections, Random}
import scala.annotation.tailrec
import scala.collection.JavaConversions._

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j

import utilities.validations.ValidationsUtilTrait


/** Class to iterate over training data and create datasets.
 *
 * @constructor Create a new action generator iterator with the given data, minibatch size and example length.
 * @param miniBatchSize Size of the minibatch.
 * @param exampleLength Lenght of each example created.
 * @param data Data to iterate over.
 */
class ActionGeneratorIterator(miniBatchSize: Int, exampleLength: Int,
                              data: Array[String]) extends DataSetIterator with ValidationsUtilTrait {

  val values: util.List[String] = getValues(0, new util.ArrayList[String]())

  val offsetsAndSize: util.TreeMap[Int, Int] = new util.TreeMap[Int, Int]()
  initializeOffsets(0, 0, offsetsAndSize)

  val keys: util.LinkedList[Int] = new util.LinkedList[Int]()
  getKeys(keys)

  val notImplementedError = "Not implemented yet"


  @tailrec
  private def getValues(idx: Int, values: util.ArrayList[String]): util.List[String] = {
    if (idx < data.length) {
      if (data(idx).length > 2) {
        values.add(data(idx))
      }
      getValues(idx + 1, values)
    }
    else {
      values
    }
  }


  @tailrec
  private def initializeOffsets(idx: Int, count: Int, map: util.TreeMap[Int, Int]): util.TreeMap[Int, Int] = {
    if (idx < data.length) {
      if (data(idx) == "-1") {
        if (idx != 0) {
          map.put(idx-count, count)
          initializeOffsets(idx + 1, 1, map)
        }
        else { initializeOffsets(idx + 1, 1, map) }
      }
      else { initializeOffsets(idx + 1, count + 1, map)}
    }
    else { map }
  }

  private def getKeys(keys: util.LinkedList[Int]): util.LinkedList[Int] = {
    val keyset = new util.LinkedList[Int](offsetsAndSize.keySet())
    keyset.foreach( set => keys.add(set))
    val seed = 12345
    Collections.shuffle(keys, new Random(seed))
    keys
  }

  /** Get next dataset from training data.
   *
   * @param num Size of next dataset.
   * @return Dataset.
   */
  override def next(num: Int): DataSet = {
    checkNotEmptyLinkedList(keys)
    val currMiniBatchSize = Math.min(num, keys.size())

    val input: INDArray = Nd4j.create(Array[Long](currMiniBatchSize+1, 3, exampleLength), 'f')
    val labels: INDArray = Nd4j.create(Array[Long](currMiniBatchSize+1, 3, exampleLength), 'f')

    val idx = 0
    createDataSet(input, labels, currMiniBatchSize, idx)
    new DataSet(input, labels)
  }

  @tailrec
  private def createDataSet(input: INDArray, labels: INDArray, currMiniBatchSize: Int, idx: Int): Unit = {
    if (idx < currMiniBatchSize) {
      val startIdx = keys.removeFirst()
      val currMiniBatchNumberOfElements = offsetsAndSize.get(startIdx)
      val endIdx = startIdx + currMiniBatchNumberOfElements

      if (startIdx < values.length) {
        val firstLine = values.get(startIdx).split(",")
        val nextIdx = startIdx + 1
        val c = 0
        fillArrays(input, labels, firstLine, idx, nextIdx, endIdx, c)
        createDataSet(input, labels, currMiniBatchSize, idx + 1)
      }
    }
  }

  @tailrec
  private def fillArrays(input: INDArray, labels: INDArray, line: Array[String],
                         idx: Int, secondIdx: Int, endIdx: Int, c: Int): Unit = {
    if (secondIdx < endIdx && secondIdx < values.length) {
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
   * @return The number of input columns.
   */
  override def inputColumns(): Int = 3

  /**
   * @return The number of total outcomes.
   */
  override def totalOutcomes(): Int = 3

  /**
   * @return True because reset is supported.
   */
  override def resetSupported(): Boolean = true

  /**
   * @return True because  async is supported.
   */
  override def asyncSupported(): Boolean = true

  /** Function that resets the iterator cleaning variables. */
  override def reset(): Unit = {
    keys.clear()
    offsetsAndSize.clear()
    initializeOffsets(0, 0, offsetsAndSize)
    getKeys(keys)
  }

  /**
   * @return Mini batch size.
   */
  override def batch(): Int = miniBatchSize

  /**
   * @return If there is more datasets available for the current training data.
   */
  override def hasNext: Boolean = keys.size() > 0

  /**
   * @return The next dataset available.
   */
  override def next(): DataSet = next(miniBatchSize)

  /**
   * @throws UnsupportedOperationException because it is not implemented.
   * @param dataSetPreProcessor DataSetPreProcessor.
   */
  override def setPreProcessor(dataSetPreProcessor: DataSetPreProcessor): Unit =
    throw new UnsupportedOperationException(notImplementedError)

  /**
   * @throws UnsupportedOperationException because it is not implemented.
   */
  override def getPreProcessor: DataSetPreProcessor =
    throw new UnsupportedOperationException(notImplementedError)

  /**
   * @throws UnsupportedOperationException because it is not implemented
   */
  override def getLabels: util.List[String] =
    throw new UnsupportedOperationException(notImplementedError)
}
