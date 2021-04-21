package neuralNetworks.rnnActionGenerator

import java.io.{FileInputStream, FileNotFoundException}
import java.util
import java.util.concurrent.TimeUnit
import java.util.{Properties, Random}

import app.twitterAPI.ConfigRun
import neuralNetworks.rnnCharacterGenerator.MainNNCharacterGenerator.{getSplitSymbol, logger}
import neuralNetworks.{NeuralNetworkConfItem, NeuralNetworkTrainingConfItem, NeuralNetworkTrainingTrait}
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
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import utilities.console.ConsoleUtilitiesTrait
import utilities.properties.PropertiesReaderUtilTrait

import scala.annotation.tailrec

object MainNNActionGenerator extends Logging with ConsoleUtilitiesTrait with PropertiesReaderUtilTrait with
NeuralNetworkTrainingTrait {

  def main(args: Array[String]): Unit = {

    val startTime = System.currentTimeMillis()

    val conf = new ConfigRun(args)

    val twitterUsernameMsg: String = "Type in twitter username to imitate"
    val twitterUsername: String = askForTwitterUsername(conf, twitterUsernameMsg)

    // Neural network conf parameters
    val confItem: NeuralNetworkConfItem = createNeuralNetworkConfItem(getProperties)
    // Training conf parameters
    val trainingConfItem: NeuralNetworkTrainingConfItem = createNeuralNetworkTrainingConfItem(getProperties)

    // Reading data and creating training and test iterators
//    val dataSetPath: String = "/data(generated)/" + twitterUsername + ".csv"
//    val data: String = IOUtils.toString(new FileInputStream(dataSetPath), "UTF-8")
//    val splitData = data.split(getSplitSymbol)
    val splitData = getData(twitterUsername, false)

    val splitSize: Int = (splitData.length * 80) / 100
    val trainingData = getTrainingData(splitData, splitSize)
    val testingData = getTestData(splitData, splitSize)

    val trainingIter = new ActionGeneratorIterator(trainingConfItem.miniBatchSize,
                                                   trainingConfItem.exampleLength,
                                                   trainingData)
    val testIter = new ActionGeneratorIterator(trainingConfItem.miniBatchSize,
                                               trainingConfItem.exampleLength,
                                               testingData)

    // Configure and create network
    val nIn = trainingIter.inputColumns()
    val nOut = trainingIter.totalOutcomes()
    val netConf: MultiLayerConfiguration = configureNetwork(confItem, nIn, nOut)
    val net = new MultiLayerNetwork(netConf)
    net.init()
    net.setListeners(new ScoreIterationListener(1))
    logger.debug(net.summary())


    val maxEpochNumber = 1000
    val maxTimeAmount = 300
    val esConf: EarlyStoppingConfiguration[MultiLayerNetwork] = new EarlyStoppingConfiguration.Builder()
      .epochTerminationConditions(new MaxEpochsTerminationCondition(maxEpochNumber))
      .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(maxTimeAmount, TimeUnit.MINUTES))
      .scoreCalculator(new DataSetLossCalculator(testIter, true))
      .evaluateEveryNEpochs(1)
      .modelSaver(new LocalFileModelSaver("./models/"))
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
    val regEval = evaluateNet(bestModel, testIter)
    logger.debug(getSplitSymbol + regEval.stats())


    val networkPath: String = "./models/" + twitterUsername + "Action.zip"
    saveNetwork(bestModel, networkPath)

    val endTime = System.currentTimeMillis()
    val timeElapsed = endTime - startTime
    logger.info("Execution time in seconds: " + timeElapsed/1000.0)

  }

  private def getData(twitterUsername: String, isTextFile: Boolean): Array[String] = {
    try {
      val dataSetFileName: String = "./data(generated)/" + twitterUsername + getFormat(isTextFile)
      val data = IOUtils.toString(new FileInputStream(dataSetFileName), "UTF-8")
      data.split(getSplitSymbol)
    }
    catch {
      case exception: FileNotFoundException => {
        logger.info(exception.getMessage)
        System.exit(1)
        new Array[String](1)
      }
    }
  }
  private def getFormat(isTextFile: Boolean): String = {
    if (isTextFile) { ".txt" } else { ".csv" }
  }
  /**
   * Private function for creating the neural network configuration item.
   * @param properties, Properties. Object containing the info of the properties file to configure the neural network.
   * @return NeuralNetworkConfItem. Item with the configuration parameters for the neural network.
   */
  private def createNeuralNetworkConfItem(properties: Properties): NeuralNetworkConfItem = {
    neuralNetworks.NeuralNetworkConfItem(
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
   * Private function that starts the tailrec function to evaluate the trained neural network.
   * @param net, MultiLayerNetwork. Network used to generate the output that will be compared.
   * @param testIter, NeuralNetworkCSVReaderIterator. Iterator for getting testing data for the neural network.
   * @return RegressionEvaluation. Object with the stats of the regression evaluation performed.
   */
  private def evaluateNet(net: MultiLayerNetwork, testIter: ActionGeneratorIterator): RegressionEvaluation = {
    val regEval = new RegressionEvaluation(3)
    testIter.reset()
    eval(regEval, testIter, net)
    testIter.reset()
    regEval
  }

  /**
   * Private tailrec function for evaluating a given network using test data obtained from an iterator.
   * @param regEval, RegressionEvaluation. Object used to evaluate the output of a neural network.
   * @param testIter, NeuralNetworkCSVReaderIterator. Iterator for getting testing data for the neural network.
   * @param net, MultiLayerNetwork. Network used to generate the output that will be compared.
   */
  @tailrec
  private def eval(regEval: RegressionEvaluation, testIter: ActionGeneratorIterator, net: MultiLayerNetwork): Unit = {
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
   * Private function for configuring the neural network.
   * @param confItem, NeuralNetworkConfItem. Item with the configuration parameters for the neural network.
   * @param nIn, Int. Number of inputs for the first layer.
   * @param nOut, Int. Number of outputs of the last layer.
   * @return MultiLayerConfiguration. Neural network configured.
   */
  private def configureNetwork(confItem: NeuralNetworkConfItem, nIn: Int, nOut: Int): MultiLayerConfiguration = {
    val nnConf = new NeuralNetConfiguration.Builder()
      .seed(confItem.seed)
      .weightInit(confItem.weightInit)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .updater(new Adam(confItem.learningRate))
      .list()

    addLayers(nnConf, confItem, nIn, 0)

    nnConf.layer(new RnnOutputLayer.Builder(confItem.lossFunction).activation(confItem.activationRNN)
      .nIn(confItem.layerWidth).nOut(nOut)
      .dropOut(0.8)
      .build())
      // Added for testing
      .backpropType(confItem.tbpttType)
      .tBPTTForwardLength(confItem.tbpttLength)
      .tBPTTBackwardLength(confItem.tbpttLength)
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

}
