//package neuralNetworks
//
//import java.io.File
//import java.util.concurrent.TimeUnit
//
//import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration
//import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver
//import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator
//import org.deeplearning4j.earlystopping.termination.{MaxEpochsTerminationCondition, MaxTimeIterationTerminationCondition}
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
//import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
//import utilities.properties.PropertiesReaderUtilTrait
//
//import scala.annotation.tailrec
//
//trait NeuralNetworkConfTrait extends PropertiesReaderUtilTrait {
//
//  val splitSymbol: String = "\n"
//  val totalPercentage: Int = 100
//  val trainingPercentage: Int = 80
//  val testPercentage: Int = totalPercentage-trainingPercentage
//
//
//  def getTotalPercentage: Int = {
//    totalPercentage
//  }
//
//  def getTrainingPercentage: Int = {
//    trainingPercentage
//  }
//
//  def getTestPercentage: Int = {
//    testPercentage
//  }
//
//  def getSplitSymbol: String = {
//    splitSymbol
//  }
//
//  def getEsConf(maxEpochNumber: Int, maxTimeAmount: Int, testIter: DataSetIterator, saver: LocalFileModelSaver)
//  : EarlyStoppingConfiguration[MultiLayerNetwork] = {
//    new EarlyStoppingConfiguration.Builder()
//      .epochTerminationConditions(new MaxEpochsTerminationCondition(maxEpochNumber))
//      .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(maxTimeAmount, TimeUnit.MINUTES))
//      .scoreCalculator(new DataSetLossCalculator(testIter, true))
//      .evaluateEveryNEpochs(1)
//      .modelSaver(new LocalFileModelSaver("./models/"))
//      .build()
//  }
//
//  def getSaver(directory: String): LocalFileModelSaver = {
//    val dirFile: File = new File(directory)
//    dirFile.mkdir()
//    new LocalFileModelSaver(directory)
//  }
//
//
//
//  def saveNetwork(net: MultiLayerNetwork, path: String): Unit = {
////    val locationToSave = new File(getProperties.getProperty("textNNPath"))
//    net.save(new File(path), true)
//  }
//
//  /**
//   * Private function that splits dataset into training data and testing data.
//   * @param splitData, Array[String]. Read dataset to be split.
//   * @param splitSize, Int. Size of the split.
//   * @return Array[String]. Split string with the given size.
//   */
//  def getTrainingData(splitData: Array[String], splitSize: Int): Array[String] = {
//    val idx = 0
//    val arrayToReturn: Array[String] = new Array[String](splitSize)
//    addTrainingElement(arrayToReturn, idx, splitData, splitSize)
//    arrayToReturn
//  }
//  /**
//   * Private tailrec function that obtains a training string from the training dataset.
//   *
//   * @param arrayToReturn, Array[String]. Array of strings to return with the training data.
//   * @param idx, Int. Idx value for stopping the tail recursive loop when it is greater than split size.
//   * @param splitData, Array[String]. Array of strings that contains the data to be split into training and test.
//   * @param splitSize, Int. Size of the split.
//   * @return Array[String]. Array of strings containing the training data.
//   */
//  @tailrec
//  private def addTrainingElement(arrayToReturn: Array[String], idx: Int, splitData: Array[String], splitSize: Int)
//    : Array[String] = {
//    if (idx < splitSize) {
//      arrayToReturn(idx) = splitData(idx)
//      addTrainingElement(arrayToReturn, idx + 1, splitData, splitSize)
//    }
//    else {
//      arrayToReturn
//    }
//  }
//
//
//  /**
//   * Private function that returns the test data.
//   * @param splitData, Array[String]. Read dataset to be split.
//   * @param splitSize, Int.Size of the split.
//   * @return Array[String]. Split string with the given size.
//   */
//  def getTestData(splitData: Array[String], splitSize: Int) : Array[String] = {
//    val idx = 0
//    val arrayToReturn: Array[String] = new Array[String](splitData.length - splitSize)
//    addTestElement(arrayToReturn, idx, splitData, splitSize)
//    arrayToReturn
//  }
//
//  /**
//   * Private tailrec function that obtains a testing string from the testing dataset.
//   *
//   * @param arrayToReturn, Array[String]. Array of strings to return with the testing data.
//   * @param idx, Int. Idx value for stopping the tail recursive loop when it is greater than split size.
//   * @param splitData, Array[String]. Array of strings that contains the data to be split into training and test.
//   * @param splitSize, Int. Size of the split.
//   * @return Array[String]. Array of strings containing the testing data.
//   */
//  @tailrec
//  private def addTestElement(arrayToReturn: Array[String], idx: Int, splitData: Array[String], splitSize: Int): Array[String] = {
//    if (idx < splitData.length - splitSize) {
//      arrayToReturn(idx) = splitData(idx + splitSize)
//      addTestElement(arrayToReturn, idx + 1, splitData, splitSize)
//    }
//    else {
//      arrayToReturn
//    }
//  }
//
//
//}
