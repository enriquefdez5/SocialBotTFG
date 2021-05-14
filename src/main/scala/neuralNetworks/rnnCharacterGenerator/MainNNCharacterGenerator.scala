package neuralNetworks.rnnCharacterGenerator

import java.util.{Properties, Random}
import scala.annotation.tailrec


import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.nn.conf.BackpropType
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.BooleanIndexing
import org.nd4j.linalg.indexing.conditions.Conditions
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

import app.twitterAPI.ConfigRun

import neuralNetworks.{NeuralNetworkConfItem, NeuralNetworkTrainingConfItem, NeuralNetworkTrainingTrait}

import utilities.console.ConsoleUtilTrait
import utilities.properties.PropertiesReaderUtilTrait


/** Object with main method for executing main text neural network training. */
object MainNNCharacterGenerator extends Logging with ConsoleUtilTrait with PropertiesReaderUtilTrait
                                with NeuralNetworkTrainingTrait {

  /** Main method for text neural network training.
   *
   * @param args Item needed to interact with Twitter API.
   */
  def main(args: Array[String]): Unit = {

    val twitterConf = new ConfigRun(args)

    val twitterUsernameMsg: String = "Type in twitter username to imitate"
    val twitterUsername = askForTwitterUsername(twitterConf, twitterUsernameMsg)

    val confItem: NeuralNetworkConfItem = createNeuralNetworkConfItem(getProperties)
    val trainingConfItem: NeuralNetworkTrainingConfItem = createNetworkTrainingConfItem(getProperties)

    val generationInitialization = trainingConfItem.generationInitialization
    val rng = new Random(confItem.seed)

    val isText = true
    val splitData = getData(twitterUsername, isText)

    val trainingIter: CharacterGeneratorIterator = getCharacterExampleIterator(trainingConfItem.miniBatchSize,
                                                                               trainingConfItem.exampleLength,
                                                                               splitData)

    val net = createAndConfigureNetwork(trainingIter, confItem)

    val idx = 0
    fitAndSample(net, trainingIter, rng, generationInitialization, trainingConfItem, idx)

    val netType = "Text"
    createPathAndSaveNetwork(net, twitterUsername, netType)
  }


  /** Get neural network sample.
   *
   * @param initialization Initialization string.
   * @param net Neural network model.
   * @param iter Dataset iterator object.
   * @param rng Random object.
   * @param charactersToSample Number of characters to sample.
   * @return
   */
  def sampleCharactersFromNetwork(initialization: String, net: MultiLayerNetwork, iter: CharacterGeneratorIterator,
                                  rng: Random, charactersToSample: Int): String = {

    val ownInitialization: String = getCharacter(initialization)

    val initializationInput: INDArray = getInitializationInput(iter, ownInitialization)

    val sb: StringBuilder = new StringBuilder(ownInitialization)
    generateSample(net, sb, iter, rng, initializationInput, charactersToSample)

    sb.toString()
  }


  private def createNetworkTrainingConfItem(properties: Properties): NeuralNetworkTrainingConfItem = {
    NeuralNetworkTrainingConfItem(
      properties.getProperty("trainingMiniBatchSize").toInt,
      properties.getProperty("trainingExampleLength").toInt,
      properties.getProperty("trainingNEpochs").toInt,
      properties.getProperty("generateSamplesEveryNMinibatches").toInt,
      properties.getProperty("trainingNCharactersToGenerate").toInt,
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
      properties.getProperty("trainingDropOut").toDouble,
      properties.getProperty("hiddenLayerWidth").toInt,
      properties.getProperty("hiddenLayerCont").toInt
    )
  }

  @tailrec
  private def fitAndSample(net: MultiLayerNetwork, iter: CharacterGeneratorIterator, rng: Random,
                           generationInitialization: String, trainingConfItem: NeuralNetworkTrainingConfItem, idx: Int)
  : Unit = {
    val miniBatchNumber = 0
    val numEpochs = trainingConfItem.numEpochs
    if (idx < numEpochs) {
      logger.info("Epoch: " + idx)
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
      nextTraining(net, iter, miniBatchNumber + 1, trainingConfItem, rng)
    }
  }

  private def getCharacterExampleIterator(miniBatchSize: Int, exampleLength: Int,
                                  data: Array[String]): CharacterGeneratorIterator = {
    new CharacterGeneratorIterator(miniBatchSize, exampleLength, data)
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
