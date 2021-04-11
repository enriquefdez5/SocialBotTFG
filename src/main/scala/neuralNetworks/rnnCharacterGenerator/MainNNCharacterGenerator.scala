package neuralNetworks.rnnCharacterGenerator

import java.io.{FileInputStream, FileNotFoundException}
import java.util.{Properties, Random}

import app.twitterAPI.ConfigRun
import neuralNetworks.{NeuralNetworkConfItem, NeuralNetworkTrainingConfItem, NeuralNetworkTrainingTrait}
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.nn.conf.layers.{DropoutLayer, LSTM, RnnOutputLayer}
import org.deeplearning4j.nn.conf.{BackpropType, MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.BooleanIndexing
import org.nd4j.linalg.indexing.conditions.Conditions
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import utilities.console.ConsoleUtilitiesTrait
import utilities.properties.PropertiesReaderUtilTrait

import scala.annotation.tailrec

object MainNNCharacterGenerator extends Logging with ConsoleUtilitiesTrait with PropertiesReaderUtilTrait with
  NeuralNetworkTrainingTrait {

  def main(args: Array[String]): Unit = {

    // start time
    val startTime = System.currentTimeMillis()

    val twitterConf = new ConfigRun(args)
    val twitterUsername = askForTwitterUsername(twitterConf)


    // Neural network conf parameters
    val confItem: NeuralNetworkConfItem = createNeuralNetworkConfItem(getProperties)
    // Training conf parameters
    val trainingConfItem: NeuralNetworkTrainingConfItem = createNetworkTrainingConfItem(getProperties)

    // Optional character initialization. A random character is used if null. Initialization characters must all be in
    // validCharactersList.
    val generationInitialization = trainingConfItem.generationInitialization
    // Random val used with configuration seed.
    val rng = new Random(confItem.seed)
    // Valid characters used for training. Others will be removed and not used for training.

    // Reading data from file
    val splitData = getData(twitterUsername, true)

//    val splitSize = (splitData.length * getTrainingPercentage) / totalPercentage
//    val trainingData = getTrainingData(splitData, splitSize)
//    val testingData = getTestData(splitData, splitSize)


    val trainingIter: CharacterGeneratorIterator = getCharacterExampleIterator(trainingConfItem.miniBatchSize,
                                                                               trainingConfItem.exampleLength,
                                                                               rng,
                                                                               splitData)
//    val testIter: CharacterGeneratorIterator = getCharacterExampleIterator(trainingConfItem.miniBatchSize,
//                                                                           trainingConfItem.exampleLength,
//                                                                           rng,
//                                                                           testingData)

    // Configure and create network
    val nIn = trainingIter.inputColumns()
    val nOut = trainingIter.totalOutcomes()
    val conf: MultiLayerConfiguration = configureNetwork(confItem, nIn, nOut)
    val net = new MultiLayerNetwork(conf)
    net.init()
    net.setListeners(new ScoreIterationListener(1))
    logger.debug(net.summary())

    // Do training, then generate and print samples from network
    val idx = 0
    fitAndSample(net, trainingIter, rng, generationInitialization, trainingConfItem, idx)

    // Save trained network
    val networkPath = "./models/" + twitterUsername + "Text.zip"
    saveNetwork(net, networkPath)

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

  def sampleCharactersFromNetwork(initialization: String, net: MultiLayerNetwork, iter: CharacterGeneratorIterator,
                                  rng: Random, charactersToSample: Int): String = {

    // Set up initialization. If no initialization: use a random character
    val ownInitialization: String = getCharacter(initialization)

    val initializationInput: INDArray = getInitializationInput(iter, ownInitialization)

    val sb: StringBuilder = new StringBuilder(ownInitialization)
    generateSample(net, sb, iter, rng, initializationInput, charactersToSample)

    sb.toString()
  }



  private def createNetworkTrainingConfItem(properties: Properties): NeuralNetworkTrainingConfItem = {
    NeuralNetworkTrainingConfItem(
      properties.getProperty("trainingMiniBatchSize").toInt,              // Size of mini batch to use when  training
      properties.getProperty("trainingExampleLength").toInt,
      // Length of each training example sequence to use. This could certainly be increased
      properties.getProperty("trainingNEpochs").toInt,                    // Total number of training epochs
      properties.getProperty("generateSamplesEveryNMinibatches").toInt,
      // How frequently to generate samples from the network?
      // 1000 characters / 50 tbptt length: 20 parameter updates per minibatch
      properties.getProperty("trainingNCharactersToGenerate").toInt,       // Length of each sample to generate
      properties.getProperty("generationInitialization")
    )
  }
  private def createNeuralNetworkConfItem(properties: Properties): NeuralNetworkConfItem = {
    neuralNetworks.NeuralNetworkConfItem(
      properties.getProperty("trainingSeed").toInt,
      properties.getProperty("trainingLearningRate").toDouble,
      WeightInit.valueOf(properties.getProperty("trainingWeightInit")),
      LossFunction.valueOf(properties.getProperty("trainingLossFunction")),
      Activation.valueOf(properties.getProperty("trainingActivationLSTM")),
      Activation.valueOf(properties.getProperty("trainingActivationRNN")),
      properties.getProperty("trainingL2").toDouble,
      BackpropType.valueOf(properties.getProperty("trainingTbpttType")),
      properties.getProperty("trainingTbpttLength").toInt,
      // Length for truncated backpropagation through time. i.e., do parameter updates ever 50 characters
      properties.getProperty("trainingDropOut").toDouble,
      properties.getProperty("hiddenLayerWidth").toInt,                 // Number of units in each LSTM layer
      properties.getProperty("hiddenLayerCont").toInt
    )
  }


  private def configureNetwork(confItem: NeuralNetworkConfItem, nIn: Int, nOut: Int): MultiLayerConfiguration = {
    val nnConf = new NeuralNetConfiguration.Builder()
      .seed(confItem.seed)
      .l2(confItem.l2)
      .weightInit(confItem.weightInit)
      .updater(new Adam(confItem.learningRate))
      .list()

    val idx = 0
    addLayers(nnConf, confItem, nIn, idx)

    nnConf.layer(new RnnOutputLayer.Builder(confItem.lossFunction).activation(confItem.activationRNN)
      // MCXENT + softmax for classification
      .nIn(confItem.layerWidth).nOut(nOut).build())
      .backpropType(confItem.tbpttType)
      .tBPTTForwardLength(confItem.tbpttLength)
      .tBPTTBackwardLength(confItem.tbpttLength)
      .build()
  }

  @tailrec
  private def addLayers(nnConf: NeuralNetConfiguration.ListBuilder, confItem: NeuralNetworkConfItem,
                        nIn: Int, idx: Int): Unit = {

    if (idx < confItem.layerCount) {
      nnConf.layer(new LSTM.Builder().nIn(nIn).nOut(confItem.layerWidth)
        .activation(confItem.activationLSTM).build())
        .layer(new DropoutLayer(confItem.dropOut))
      addLayers(nnConf, confItem, nIn, idx + 1)
    }
  }

  @tailrec
  private def fitAndSample(net: MultiLayerNetwork, iter: CharacterGeneratorIterator, rng: Random,
                           generationInitialization: String, trainingConfItem: NeuralNetworkTrainingConfItem, idx: Int)
  : Unit = {
    val miniBatchNumber = 0
    val numEpochs = trainingConfItem.numEpochs
    if (idx < numEpochs) {
      logger.debug("Epoch: " + idx)
      nextTraining(net, iter, miniBatchNumber, trainingConfItem, rng)
      iter.reset()
      fitAndSample(net, iter, rng, generationInitialization, trainingConfItem, idx + 1)
    }
  }

  @tailrec
  private def nextTraining(net: MultiLayerNetwork, iter: CharacterGeneratorIterator,
                           miniBatchNumber: Int, trainingConfItem: NeuralNetworkTrainingConfItem, rng: Random): Unit = {
    if (iter.hasNext) {
      net.fit(iter.next())
//      sample(net, iter, miniBatchNumber, trainingConfItem, rng)
      nextTraining(net, iter, miniBatchNumber + 1, trainingConfItem, rng)
    }
  }

  private def sample(net: MultiLayerNetwork, iter: CharacterGeneratorIterator,
                     miniBatchNumber: Int, trainingConfItem: NeuralNetworkTrainingConfItem, rng: Random): Unit = {
    val lineBreak = "\n"
    if (miniBatchNumber % trainingConfItem.generateSamplesEveryNMinibatches == 0) {
      logger.debug("--------------------")
      logger.debug("Completed " + miniBatchNumber + " minibatches of size " + trainingConfItem.miniBatchSize + "x" +
        trainingConfItem.exampleLength +
        " " +
        "characters")
      logger.debug("Sampling characters from network given initialization \"" + (
        if (trainingConfItem.generationInitialization == null) {
          ""
        }
        else {
          trainingConfItem.generationInitialization
        }
        ) + "\"")
      val samples: String = sampleCharactersFromNetwork(trainingConfItem.generationInitialization, net, iter, rng,
        trainingConfItem.nCharactersToSample)
      logger.debug("----- Generated sample -----")
      logger.debug(samples + lineBreak)
    }
  }
  private def getCharacterExampleIterator(miniBatchSize: Int, exampleLength: Int, rng: Random,
                                  data: Array[String]): CharacterGeneratorIterator = {
    new CharacterGeneratorIterator(miniBatchSize, exampleLength, rng, data)
  }

  private def getCharacter(initialization: String): String = {
    if (initialization == null || initialization == "") {
      "a"
    }
    else {
      initialization
    }
  }

  private def generateSample(net: MultiLayerNetwork, sb: StringBuilder,
                             iter: CharacterGeneratorIterator, rng: Random,
                             initializationInput: INDArray, charactersToSample: Int): Unit = {
    net.rnnClearPreviousState()
    val output = net.rnnTimeStep(initializationInput)
    val tensorOutput = output.tensorAlongDimension(output.size(2)-1, 1, 0)

    val idx = 0
    buildSample(net, idx, charactersToSample, iter, tensorOutput, rng, sb)
  }

  @tailrec
  private def buildSample(net: MultiLayerNetwork, idx: Int, charactersToSample: Int, iter: CharacterGeneratorIterator,
                          output: INDArray, rng: Random, sb: StringBuilder): Unit = {
    if (idx < charactersToSample) {
      val nextInput: INDArray = Nd4j.zeros(1, iter.inputColumns().toLong)
      val cumsum = Nd4j.cumsum(output, 1)
      val sampledCharacterIdx = BooleanIndexing.firstIndex(cumsum.getRow(0), Conditions.greaterThan(rng.nextDouble())
      ).getInt(0)
      nextInput.putScalar(Array[Int](0, sampledCharacterIdx), 1.0f)
      sb.append(iter.convertIndexToChar(sampledCharacterIdx))

      buildSample(net, idx + 1, charactersToSample, iter, net.rnnTimeStep(nextInput), rng, sb)
    }
  }

  private def getInitializationInput(iter: CharacterGeneratorIterator, ownInitialization: String): INDArray = {
    val initializationToReturn: INDArray = Nd4j.zeros(1, iter.inputColumns(), ownInitialization.length)
    val init: Array[Char] = ownInitialization.toCharArray
    val idx = 0
    addCharToArray(init, idx, iter, initializationToReturn)
    initializationToReturn
  }

  @tailrec
  private def addCharToArray(init: Array[Char], idx: Int,
                             iter: CharacterGeneratorIterator, initializationToReturn: INDArray): Unit = {
    if (idx < init.length) {
      val idxToAdd = iter.convertCharacterToIndex(init(idx))
      initializationToReturn.putScalar(Array[Int](1, idxToAdd, idx), 1.0f)
      addCharToArray(init, idx + 1, iter, initializationToReturn)
    }
  }

}
