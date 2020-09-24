package rnn

import java.io.{File, FileInputStream}
import java.util.{Properties, Random}

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

import scala.annotation.tailrec

object MainDl4jExample extends Logging {
  def main(args: Array[String]): Unit = {
    // Read properties file
    val properties: Properties = new Properties()
    properties.load(new FileInputStream("src/main/resources/config.properties"))
    // Conf parameters
    val confItem: NeuralNetworkConfItem = NeuralNetworkConfItem(
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

    // Optional character initialization. A random character is used if null. Initilization characters must all be in
    // validCharactersList.
    val generationInitialization = null
    // Random val used with configuration seed.
    val rng = new Random(confItem.seed)
    // Valid characters used for training. Others will be removed and not used for training.

    // Reading data from file
    val data = IOUtils.toString(new FileInputStream("dataSet.txt"), "UTF-8")
    val iter: CharacterIterator = getCharacterExampleIterator(confItem.miniBatchSize, confItem.exampleLength, rng, data)
    val nIn = iter.inputColumns()
    val nOut = iter.totalOutcomes()

    // Set up network configuration:
    val conf: MultiLayerConfiguration = configureNetwork(confItem, nIn, nOut)
    val net = new MultiLayerNetwork(conf)
    net.init()
    net.setListeners(new ScoreIterationListener(1))
    logger.debug(net.summary())

    // Do training, and then generate and print samples from network
    val idx = 0
    fitAndSample(net, iter, rng, generationInitialization, confItem, idx)

    // Save trained network
    val locationToSave = new File("daksjdasdasd.zip")
    net.save(locationToSave, true)
  }


  private def configureNetwork(confItem: NeuralNetworkConfItem, nIn: Int, nOut: Int): MultiLayerConfiguration = {
    new NeuralNetConfiguration.Builder()
      .seed(confItem.seed)
      .l2(confItem.l2)
      .weightInit(confItem.weightInit)
      .updater(new Adam(confItem.learningRate))
      .list()
      .layer(new LSTM.Builder().nIn(nIn).nOut(confItem.layerWidth)
        .activation(confItem.activationLSTM).build())
      .layer(new DropoutLayer(confItem.dropOut))
      .layer(new LSTM.Builder().nIn(confItem.layerWidth).nOut(confItem.layerWidth)
        .activation(confItem.activationLSTM).build())
      .layer(new DropoutLayer(confItem.dropOut))
      .layer(new RnnOutputLayer.Builder(confItem.lossFunction).activation(confItem.activationRNN)
        // MCXENT + softmax for classification
        .nIn(confItem.layerWidth).nOut(nOut).build())
      .backpropType(confItem.tbpttType)
      .tBPTTForwardLength(confItem.tbpttLength)
      .tBPTTBackwardLength(confItem.tbpttLength)
      .build()
  }

  @tailrec
  private def fitAndSample(net: MultiLayerNetwork, iter: CharacterIterator, rng: Random,
                           generationInitialization: String, confItem: NeuralNetworkConfItem, idx: Int): Unit = {
    val miniBatchNumber = 0
    val numEpochs = confItem.numEpochs
    if (idx < numEpochs) {
      logger.debug("Epoch: " + idx)
      nextTraining(net, iter, miniBatchNumber, confItem, rng)
      iter.reset()
      fitAndSample(net, iter, rng, generationInitialization, confItem, idx + 1)
    }
  }

  @tailrec
  private def nextTraining(net: MultiLayerNetwork, iter: CharacterIterator,
                           miniBatchNumber: Int, confItem: NeuralNetworkConfItem, rng: Random): Unit = {
    if (iter.hasNext) {
      net.fit(iter.next())
      sample(net, iter, miniBatchNumber, confItem, rng)
      nextTraining(net, iter, miniBatchNumber + 1, confItem, rng)
    }
  }

  private def sample(net: MultiLayerNetwork, iter: CharacterIterator,
                     miniBatchNumber: Int, confItem: NeuralNetworkConfItem, rng: Random): Unit = {
    val lineBreak = "\n"
    if (miniBatchNumber % confItem.generateSamplesEveryNMinibatches == 0) {
      logger.debug("--------------------")
      logger.debug("Completed " + miniBatchNumber + " minibatches of size " + confItem.miniBatchSize + "x" +
        confItem.exampleLength +
        " " +
        "characters")
      logger.debug("Sampling characters from network given initialization \"" + (
        if (confItem.generationInitialization == null) {
          ""
        }
        else {
          confItem.generationInitialization
        }
        ) + "\"")
      val samples: String = sampleCharactersFromNetwork(confItem.generationInitialization, net, iter, rng,
        confItem.nCharactersToSample)
      logger.debug("----- Sample -----")
      logger.debug(samples.toString + lineBreak)
      logger.debug("----- Another sample ------")
      val anotherSample: String = sampleCharactersFromNetwork("El gobierno y ", net, iter, rng,
        confItem.nCharactersToSample)
      logger.debug(anotherSample.toString + lineBreak)
      logger.debug("----- And another one ------")
      val anotherOne: String = sampleCharactersFromNetwork("El puto Reven es un ", net, iter, rng,
        confItem.nCharactersToSample)
      logger.debug(anotherOne.toString + lineBreak)
    }
  }
  private def getCharacterExampleIterator(miniBatchSize: Int, exampleLength: Int, rng: Random,
                                  data: String): CharacterIterator = {
    new CharacterIterator(miniBatchSize, exampleLength, rng, data)
  }

  private def getCharacter(initialization: String): String = {
    if (initialization == null) {
      "a"
    }
    else {
      initialization
    }
  }

  def sampleCharactersFromNetwork(initialization: String, net: MultiLayerNetwork, iter: CharacterIterator,
    rng: Random, charactersToSample: Int): String = {

    // Set up initialization. If no initialization: use a random character
    val ownInitialization: String = getCharacter(initialization)

    val initializationInput: INDArray = getInitializationInput(iter, ownInitialization)


    val sb: StringBuilder = new StringBuilder(ownInitialization)
    generateSample(net, sb, iter, rng, initializationInput, charactersToSample)


    sb.toString()
  }

  private def generateSample(net: MultiLayerNetwork, sb: StringBuilder,
                             iter: CharacterIterator, rng: Random,
                             initializationInput: INDArray, charactersToSample: Int): Unit = {
    net.rnnClearPreviousState()
    val output = net.rnnTimeStep(initializationInput)
    val tensorOutput = output.tensorAlongDimension(output.size(2)-1, 1, 0)

    val idx = 0
    buildSample(net, idx, charactersToSample, iter, tensorOutput, rng, sb)
  }

  @tailrec
  private def buildSample(net: MultiLayerNetwork, idx: Int, charactersToSample: Int, iter: CharacterIterator,
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

  private def getInitializationInput(iter: CharacterIterator, ownInitialization: String): INDArray = {
    val initializationToReturn: INDArray = Nd4j.zeros(1, iter.inputColumns(), ownInitialization
      .length)
    val init: Array[Char] = ownInitialization.toCharArray
    val idx = 0
    addCharToArray(init, idx, iter, initializationToReturn)
    initializationToReturn
  }

  @tailrec
  private def addCharToArray(init: Array[Char], idx: Int,
                             iter: CharacterIterator, initializationToReturn: INDArray): Unit = {
    if (idx < init.length) {
      val idxToAdd = iter.convertCharacterToIndex(init(idx))
      initializationToReturn.putScalar(Array[Int](1, idxToAdd, idxToAdd), 1.0f)
      addCharToArray(init, idx + 1, iter, initializationToReturn)
    }
  }

}
