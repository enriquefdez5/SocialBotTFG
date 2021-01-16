package twitterActionRNN

import java.io.{BufferedWriter, File, FileInputStream, FileWriter, IOException}
import java.util
import java.util.concurrent.TimeUnit
import java.util.{Properties, Random}

import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.earlystopping.{EarlyStoppingConfiguration, EarlyStoppingResult}
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator
import org.deeplearning4j.earlystopping.termination.{MaxEpochsTerminationCondition, MaxTimeIterationTerminationCondition}
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.{LSTM, RnnOutputLayer}
import org.deeplearning4j.nn.conf.{BackpropType, MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.evaluation.regression.RegressionEvaluation
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

import scala.annotation.tailrec

object NeuralNetworkCSVReader extends Logging {



  def main(args: Array[String]): Unit = {
    val properties: Properties = new Properties()
    properties.load(new FileInputStream("src/main/resources/config.properties"))

    // Neural network conf parameters
    val confItem: NeuralNetworkConfItem = createNeuralNetworkConfItem(properties)
    // Training conf parameters
    val trainingConfItem: NeuralNetworkTrainingConfItem = createNeuralNetworkTrainingConfItem(properties)


    // Reading data and creating training and test iterators
    val data: String = IOUtils.toString(new FileInputStream("trainCSV.csv"), "UTF-8")
    val splitData = data.split(getSplitSymbol)
    val splitSize: Int = (splitData.length * 80) / 100
    val trainingData = getTrainingData(splitData, splitSize)
    val testingData = getTestData(splitData, splitSize)

    val trainingIter = new NeuralNetworkCSVReaderIterator(trainingConfItem.miniBatchSize,
                                                          trainingConfItem.exampleLength,
                                                          trainingData)
    val testIter = new NeuralNetworkCSVReaderIterator(trainingConfItem.miniBatchSize,
                                                      trainingConfItem.exampleLength,
                                                      testingData)

    val rng = new Random(confItem.seed)
    val nIn = trainingIter.inputColumns()
    val nOut = trainingIter.totalOutcomes()

    // Configure and create network
    val netConf: MultiLayerConfiguration = configureNetwork(confItem, nIn, nOut)
    val net = new MultiLayerNetwork(netConf)
    net.init()
    net.setListeners(new ScoreIterationListener(1))
    logger.debug(net.summary())

    // ---------------------------
    val maxEpochNumber = 1000
    val maxTimeAmount = 960
    val esConf: EarlyStoppingConfiguration[MultiLayerNetwork] = new EarlyStoppingConfiguration.Builder()
      .epochTerminationConditions(new MaxEpochsTerminationCondition(maxEpochNumber))
      .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(maxTimeAmount, TimeUnit.MINUTES))
      .scoreCalculator(new DataSetLossCalculator(testIter, true))
      .evaluateEveryNEpochs(1)
      .modelSaver(new LocalFileModelSaver("earlyStop.zip"))
      .build()

    val trainer: EarlyStoppingTrainer = new EarlyStoppingTrainer(esConf, net, trainingIter)

    val result: EarlyStoppingResult[MultiLayerNetwork] = trainer.fit()

    logger.debug("Termination reason: " + result.getTerminationReason)
    logger.debug("Termination details: " + result.getTerminationDetails)
    logger.debug("Total epochs: " + result.getTotalEpochs)
    logger.debug("Best epoch number: " + result.getBestModelEpoch)
    logger.debug("Score at best epoch: " + result.getBestModelScore)


    val bestModel: MultiLayerNetwork = result.getBestModel

    // Evaluate best model obtained with test data.
    evaluateNet(bestModel, testIter)

    val locationToSave = new File("esNet.zip")
    bestModel.save(locationToSave)

    // ---------------------------
//
//
//    // Do training, then generate and print samples from network
//    val idx = 0
//    val scores = new util.ArrayList[String]()
//    fitAndSample(net, trainingIter, testIter, rng, trainingConfItem, idx, scores)
//
//    // Evaluate with test data
//    evaluateNet(net, testIter)
//
//    // Save network
//    val locationToSave = new File("nnWorkingTest.zip")
//    net.save(locationToSave)
//
//    // Save training scores for future logging
//    writeScores(scores, "./trainingScores.txt")
  }

  /**
   * Private tailrec function for traditional training without early stopping. It is used when no early stopping training is configured.
   * @param net, MultiLayerNetwork. Network to be trained.
   * @param iter, NeuralNetworkCSVReaderIterator. Iterator for getting training data for the neural network.
   * @param testIter, NeuralNetworkCSVReaderIterator. Iterator for getting testing data for the neural network
   * @param rng, Random. Random object for setting random seed.
   * @param trainingConfItem, NeuralNetworkTrainingConfItem. Object with the params needed to train the neural network.
   * @param idx, Int. Index value for iterating n number of Epochs.
   * @param scores, ArrayList[String]. List of strings with the training score of each epoch.
   */
  @tailrec
  private def fitAndSample(net: MultiLayerNetwork,
                           iter: NeuralNetworkCSVReaderIterator,
                           testIter: NeuralNetworkCSVReaderIterator,
                           rng: Random,
                           trainingConfItem: NeuralNetworkTrainingConfItem,
                           idx: Int,
                           scores: util.ArrayList[String]): Unit = {
    val numEpochs = trainingConfItem.numEpochs
    if (idx < numEpochs) {
      logger.debug("Epoch: " + idx)
      nextTraining(net, iter, trainingConfItem, rng)
      val score = net.score()
      logger.debug("Score is " + score.toString)
      scores.add(score.toString + getSplitSymbol)
      iter.reset()
      generateSample(idx, trainingConfItem, net)
      fitAndSample(net, iter, testIter, rng, trainingConfItem, idx + 1, scores)
    }
  }

  /**
   * Private tailrec function for training while there is more data in the training iterator.
   * @param net, MultiLayerNetwork. Network to be trained.
   * @param iter, NeuralNetworkCSVReaderIterator. Iterator for getting training data for the neural network.
   * @param trainingConfItem, NeuralNetworkTrainingConfItem. Object with the params needed to train the neural network.
   * @param rng, Random. Random object for setting random seed.
   */
  @tailrec
  private def nextTraining(net: MultiLayerNetwork,
                           iter: NeuralNetworkCSVReaderIterator,
                           trainingConfItem: NeuralNetworkTrainingConfItem,
                           rng: Random): Unit = {
    if (iter.hasNext) {
      net.fit(iter.next())
      nextTraining(net, iter, trainingConfItem, rng)
    }
  }

  /**
   * Private function that uses the network to generate samples at generateSampleEveryNMinibatches param value.
   * @param miniBatchNumber, Int. Minibatch training count value.
   * @param trainingConfItem, NeuralNetworkTrainingConfItem. Object from which generateSAmpleEveryNMinibatches param
   *                        will be obtained.
   * @param net, MultiLayerNetwork. Network that will be use to generate samples.
   */
  private def generateSample(miniBatchNumber: Int,
                             trainingConfItem: NeuralNetworkTrainingConfItem,
                             net: MultiLayerNetwork): Unit = {
    if (miniBatchNumber % trainingConfItem.generateSampleEveryNMinibatches == 0) {
      net.rnnClearPreviousState()

      val inputArray: INDArray = Nd4j.create(Array[Long](1, 3, 1), 'f')
      inputArray.putScalar(Array[Long](0, 0, 0), 5.0/7.0)
      inputArray.putScalar(Array[Long](0, 1, 0), 15.0/23.0)
      inputArray.putScalar(Array[Long](0, 2, 0), 1.0/3.0)
      logger.debug(net.rnnTimeStep(inputArray).toString())

      net.rnnClearPreviousState()

      val anotherInputArray: INDArray = Nd4j.create(Array[Long](1, 3, 1), 'f')
      anotherInputArray.putScalar(Array[Long](0, 0, 0), 2.0/7.0)
      anotherInputArray.putScalar(Array[Long](0, 1, 0), 9.0/23.0)
      anotherInputArray.putScalar(Array[Long](0, 2, 0), 1.0/3.0)
      logger.debug(net.rnnTimeStep(anotherInputArray).toString())

      net.rnnClearPreviousState()

      val anotherInputArray2: INDArray = Nd4j.create(Array[Long](1, 3, 1), 'f')
      anotherInputArray2.putScalar(Array[Long](0, 0, 0), 7.0/7.0)
      anotherInputArray2.putScalar(Array[Long](0, 1, 0), 19.0/23.0)
      anotherInputArray2.putScalar(Array[Long](0, 2, 0), 1.0/3.0)
      logger.debug(net.rnnTimeStep(anotherInputArray2).toString())
    }
  }

  /**
   * Private tailrec function for evaluating a given network using test data obtained from an iterator.
   * @param regEval, RegressionEvaluation. Object used to evaluate the output of a neural network.
   * @param testIter, NeuralNetworkCSVReaderIterator. Iterator for getting testing data for the neural network.
   * @param net, MultiLayerNetwork. Network used to generate the output that will be compared.
   */
  @tailrec
  private def eval(regEval: RegressionEvaluation, testIter: NeuralNetworkCSVReaderIterator, net: MultiLayerNetwork): Unit = {
    if (testIter.hasNext) {
      net.rnnClearPreviousState()
      val testds = testIter.next()
      val prediction: INDArray = net.rnnTimeStep(testds.getFeatures)
      regEval.evalTimeSeries(testds.getLabels, prediction)
      logger.debug(regEval.stats())
      eval(regEval, testIter, net)
    }
  }

  /**
   * Private function that starts the tailrec function to evaluate the trained neural network.
   * @param net, MultiLayerNetwork. Network used to generate the output that will be compared.
   * @param testIter, NeuralNetworkCSVReaderIterator. Iterator for getting testing data for the neural network.
   * @return RegressionEvaluation. Object with the stats of the regression evaluation performed.
   */
  private def evaluateNet(net: MultiLayerNetwork, testIter: NeuralNetworkCSVReaderIterator): RegressionEvaluation = {
    val regEval = new RegressionEvaluation(3)
    eval(regEval, testIter, net)
    logger.debug(getSplitSymbol + regEval.stats())
    testIter.reset()
    regEval
  }


  /**
   * Private function that splits dataset into training data and testing data.
   * @param splitData, Array[String]. Read dataset to be split.
   * @param splitSize, Int. Size of the split.
   * @return Array[String]. Split string with the given size.
   */
  private def getTrainingData(splitData: Array[String], splitSize: Int): Array[String] = {
    val idx = 0
    val arrayToReturn: Array[String] = new Array[String](splitSize)
    addTrainingElement(arrayToReturn, idx, splitData, splitSize)
    arrayToReturn
  }

  /**
   * Private tailrec function that obtains a training string from the training dataset.
   *
   * @param arrayToReturn, Array[String]. Array of strings to return with the training data.
   * @param idx, Int. Idx value for stopping the tail recursive loop when it is greater than split size.
   * @param splitData, Array[String]. Array of strings that contains the data to be split into training and test.
   * @param splitSize, Int. Size of the split.
   * @return Array[String]. Array of strings containing the training data.
   */
  @tailrec
  private def addTrainingElement(arrayToReturn: Array[String], idx: Int, splitData: Array[String], splitSize: Int): Array[String] = {
    if (idx < splitSize) {
      arrayToReturn(idx) = splitData(idx)
      addTrainingElement(arrayToReturn, idx + 1, splitData, splitSize)
    }
    else {
      arrayToReturn
    }
  }


  /**
   * Private function that returns the test data.
   * @param splitData, Array[String]. Read dataset to be split.
   * @param splitSize, Int.Size of the split.
   * @return Array[String]. Split string with the given size.
   */
  private def getTestData(splitData: Array[String], splitSize: Int) : Array[String] = {
    val idx = 0
    val arrayToReturn: Array[String] = new Array[String](splitData.length - splitSize)
    addTestElement(arrayToReturn, idx, splitData, splitSize)
    arrayToReturn
  }

  /**
   * Private tailrec function that obtains a testing string from the testing dataset.
   *
   * @param arrayToReturn, Array[String]. Array of strings to return with the testing data.
   * @param idx, Int. Idx value for stopping the tail recursive loop when it is greater than split size.
   * @param splitData, Array[String]. Array of strings that contains the data to be split into training and test.
   * @param splitSize, Int. Size of the split.
   * @return Array[String]. Array of strings containing the testing data.
   */
  @tailrec
  private def addTestElement(arrayToReturn: Array[String], idx: Int, splitData: Array[String], splitSize: Int): Array[String] = {
    if (idx < splitData.length - splitSize) {
      arrayToReturn(idx) = splitData(idx + splitSize)
      addTestElement(arrayToReturn, idx + 1, splitData, splitSize)
    }
    else {
      arrayToReturn
    }
  }

  /**
   * Private function for creating the neural network configuration item.
   * @param properties, Properties. Object containing the info of the properties file to configure the neural network.
   * @return NeuralNetworkConfItem. Item with the configuration parameters for the neural network.
   */
  private def createNeuralNetworkConfItem(properties: Properties): NeuralNetworkConfItem = {
    NeuralNetworkConfItem(
      properties.getProperty("trainingActionRandomSeed").toInt,
      properties.getProperty("trainingActionLearningRate").toDouble,
      WeightInit.valueOf(properties.getProperty("trainingActionWeightInit")),
      LossFunction.valueOf(properties.getProperty("trainingActionLossFunction")),
      Activation.valueOf(properties.getProperty("trainingActionActivationLSTM")),
      Activation.valueOf(properties.getProperty("trainingActionActivationRNN")),
      properties.getProperty("trainingActionL2").toDouble,
      BackpropType.valueOf(properties.getProperty("trainingActionTbpttType")),
      properties.getProperty("trainingActionTbpttLength").toInt,
      properties.getProperty("trainingActionDropout").toDouble,
      properties.getProperty("trainingActionHiddenLayerWidth").toInt,
      properties.getProperty("trainingActionHiddenLayerCont").toInt
    )
  }

  /**
   * Private function for creating the neural network training configuration item.
   * @param properties, Properties. Object containing the info of the properties file to configure the neural network
   *                  training.
   * @return NeuralNetworkTrainingConfItem. Item with the configuration parameters for the neural network training.
   */
  private def createNeuralNetworkTrainingConfItem(properties: Properties): NeuralNetworkTrainingConfItem = {
    NeuralNetworkTrainingConfItem(
      properties.getProperty("trainingActionMiniBatchSize").toInt,
      properties.getProperty("trainingActionExampleLength").toInt,
      properties.getProperty("trainingActionNEpochs").toInt,
      properties.getProperty("trainingActionGenerateSamplesEveryNMiniBatches").toInt,
      properties.getProperty("trainingActionNCharactersToGenerate").toInt,
      properties.getProperty("trainingActionInitialization")
    )
  }


  /**
   * Private function for configuring the neural network.
   * @param confItem, NeuralNetworkConfItem. Item with the configuration parameters for the neural network.
   * @param nIn, Int. Number of inputs for the first layer.
   * @param nOut, Int. Number of outputs of the last layer.
   * @return, MultiLayerConfiguration. Neural network configured.
   */
  private def configureNetwork(confItem: NeuralNetworkConfItem, nIn: Int, nOut: Int): MultiLayerConfiguration = {
    val nnConf = new NeuralNetConfiguration.Builder()
      .seed(confItem.seed)
      .weightInit(confItem.weightInit)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .updater(new Adam(confItem.learningRate))
//      .l2(confItem.l2)
      .list()

    val idx = 0
    addLayers(nnConf, confItem, nIn, idx)

    nnConf.layer(new RnnOutputLayer.Builder(confItem.lossFunction).activation(confItem.activationRNN)
      .nIn(confItem.layerWidth).nOut(nOut)
//      .dropOut(confItem.dropOut)
      .build())
//      .backpropType(confItem.tbpttType)
//      .tBPTTBackwardLength(confItem.tbpttLength)
//      .tBPTTForwardLength(confItem.tbpttLength)
      .build()
  }

  /**
   * Private tailrec function that add and configure layers for the neural network.
   * @param conf, NeuralNetConfiguration.ListBuilder. Item to be configured with the specific layer parameters.
   * @param confItem, NeuralNetworkConfItem. Item with the configuration parameters for the neural network.
   * @param nIn, Int. Number of inputs for the layer.
   * @param idx, Int. Index value for stopping the loop when its greater than layer count value.
   */
  @tailrec
  private def addLayers(conf: NeuralNetConfiguration.ListBuilder, confItem: NeuralNetworkConfItem,
                        nIn: Int, idx: Int): Unit = {
    if (idx < confItem.layerCount) {
      conf.layer(new LSTM.Builder().nIn(nIn).nOut(confItem.layerWidth)
        .activation(confItem.activationLSTM).build())
      addLayers(conf, confItem, confItem.layerWidth, idx + 1)
    }
  }

  /**
   * Private function for logging training loss scores into a file.
   * @param scores, ArrayList[String]. List of string containing score values to be saved on a file.
   * @param fileName, String. File name where scores will be saved.
   */
  private def writeScores(scores: util.ArrayList[String], fileName: String): Unit = {
    // FileWriter
    val file = new File(fileName)
    val bw = new BufferedWriter(new FileWriter(file))

    try {
      for ( i <- 0 until scores.size()) {
        bw.write(scores.get(i))
      }
    }
    catch {
      case ioexc: IOException =>
        logger.error("Ups! Something went wrong writing on the file", ioexc)
    }
    // Closing the buffer
    bw.close()
  }

  /**
   * Private function for getting the split symbol used to split tweets.
   * @return the split symbol.
   */
  private def getSplitSymbol: String = "\n"

}
