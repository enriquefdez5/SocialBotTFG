package neuralNetworks.rnnCharacterGenerator

import java.io.{File, FileInputStream}
import java.util.concurrent.TimeUnit
import java.util.{Properties, Random}

import neuralNetworks.{NeuralNetworkConfTrait, NeuralNetworkTrainingTrait}
import org.apache.commons.io.{FilenameUtils, IOUtils}
import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.earlystopping.{EarlyStoppingConfiguration, EarlyStoppingResult}
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator
import org.deeplearning4j.earlystopping.termination.{MaxEpochsTerminationCondition, MaxTimeIterationTerminationCondition}
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer
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
import utilities.neuralNetworks.{NeuralNetworkConfItem, NeuralNetworkTrainingConfItem}
import utilities.properties.PropertiesReaderUtilTrait

import scala.annotation.tailrec

object MainNNCharacterGenerator extends Logging
                                   with PropertiesReaderUtilTrait
                                   with NeuralNetworkConfTrait
                                   with NeuralNetworkTrainingTrait {

  def main(args: Array[String]): Unit = {

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
    val data = IOUtils.toString(new FileInputStream(getProperties.getProperty("dataSetFileName")), "UTF-8")
    val splitData = data.split(getSplitSymbol)
    val splitSize = (splitData.length * getTrainingPercentage) / totalPercentage
    val trainingData = getTrainingData(splitData, splitSize)
    val testingData = getTestData(splitData, splitSize)


    val trainingIter: CharacterGeneratorIterator = getCharacterExampleIterator(trainingConfItem.miniBatchSize,
                                                                               trainingConfItem.exampleLength,
                                                                               rng,
                                                                               trainingData)
    val testIter: CharacterGeneratorIterator = getCharacterExampleIterator(trainingConfItem.miniBatchSize,
                                                                           trainingConfItem.exampleLength,
                                                                           rng,
                                                                           testingData)

    // Configure and create network
    val nIn = trainingIter.inputColumns()
    val nOut = trainingIter.totalOutcomes()
    val conf: MultiLayerConfiguration = configureNetwork(confItem, nIn, nOut)
    val net = new MultiLayerNetwork(conf)
    net.init()
    net.setListeners(new ScoreIterationListener(1))
    logger.debug(net.summary())

    // Configure early stopping
//    val directory: String = "./models/" + getProperties.getProperty("twitterUsername")
//    val saver: LocalFileModelSaver = getSaver(directory)
//
//    val maxEpochNumber = 1000
//    val maxTimeAmount = 240

//    val bestModel = fitNetwork(maxEpochNumber, maxTimeAmount, trainingIter, testIter, net, saver)
//    fitNetwork(maxEpochNumber, maxTimeAmount, trainingIter, testIter, net, saver)
//
//    val esConf: EarlyStoppingConfiguration[MultiLayerNetwork] = getEsConf(maxEpochNumber,
//                                                                          maxTimeAmount,
//                                                                          testIter,
//                                                                          saver)
//    val trainer: EarlyStoppingTrainer = new EarlyStoppingTrainer(esConf, net, trainingIter)
//    val result: EarlyStoppingResult[MultiLayerNetwork] = trainer.fit()
//
//    logger.debug("Termination reason: " + result.getTerminationReason)
//    logger.debug("Termination details: " + result.getTerminationDetails)
//    logger.debug("Total epochs: " + result.getTotalEpochs)
//    logger.debug("Best epoch number: " + result.getBestModelEpoch)
//    logger.debug("Score at best epoch: " + result.getBestModelScore)
//
//    val bestModel: MultiLayerNetwork = result.getBestModel
//
////    // Evaluate best model obtained with test data.
////    evaluateNet(bestModel, testIter)
//
//    saveNetwork(bestModel, getProperties.getProperty("textNNPath"))


//    logger.debug("\n-----------------------")
//    logger.debug("Here is a sample with initilialization string: \"Sirva este tweet \":\n")
//    val charactersToSample = 200
//    sampleCharactersFromNetwork("Sirva este tweet ", bestModel, trainingIter, rng, charactersToSample)
//    logger.debug("\n-----------------------")
//    logger.debug("Here is another sample with no initilialization string:\n")
//    sampleCharactersFromNetwork(generationInitialization, bestModel, trainingIter, rng, charactersToSample)



    // Do training, then generate and print samples from network
    val idx = 0
    fitAndSample(net, trainingIter, rng, generationInitialization, trainingConfItem, idx)

    // Save trained network
    saveNetwork(net, getProperties.getProperty("textNNPath"))
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
    NeuralNetworkConfItem(
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
      sample(net, iter, miniBatchNumber, trainingConfItem, rng)
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
