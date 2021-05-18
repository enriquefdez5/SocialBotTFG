package neuralNetworks

import java.io.{File, FileInputStream, FileNotFoundException}
import java.util.Random

import scala.annotation.tailrec

import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.conf.layers.{LSTM, RnnOutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.Adam

import model.NNActionItem.{buildNNActionItemFromDayHourAndAction, statusToNNActionItem}
import model.{NNActionItem, StatusImpl}
import model.exceptions.{NotExistingFileException, WrongParamValueException}

import neuralNetworks.rnnCharacterGenerator.CharacterGeneratorIterator
import neuralNetworks.rnnCharacterGenerator.MainNNCharacterGenerator.sampleCharactersFromNetwork

import utilities.properties.PropertiesReaderUtilTrait
import utilities.validations.ValidationsUtilTrait

/** Trait that contains more functionality for the neural network training. */
trait NeuralNetworkTrainingTrait extends Logging with ValidationsUtilTrait with PropertiesReaderUtilTrait {

  val splitSymbol: String = "\n"
  val totalPercentage: Int = 100
  val trainingPercentage: Int = 80
  val testPercentage: Int = totalPercentage-trainingPercentage

  val neuralNetworkFolderPath = "./models/"

  /** Save neural network model.
   *
   * @param net Net model to save.
   * @param twitterUsername Twitter username of the neural network.
   * @param netType Type of the neural network. It could be Action or Text
   */
  def createPathAndSaveNetwork(net: MultiLayerNetwork, twitterUsername: String, netType: String): Unit = {
    val networkPath: String = neuralNetworkFolderPath + twitterUsername + netType + ".zip"
    saveNetwork(net, networkPath)
  }

  private def saveNetwork(net: MultiLayerNetwork, path: String): Unit = {
    net.save(new File(path), true)
  }

  /** Create and configure a multilayer network.
   *
   * @param trainingIter Dataset Iterator, it could be ActionGeneratorIterator or CharacterGeneratorIterator.
   * @param confItem Configuration item with data to configure the created neural network.
   * @return Created and configured multilayer network.
   */
  def createAndConfigureNetwork(trainingIter: DataSetIterator, confItem: NeuralNetworkConfItem): MultiLayerNetwork = {
    val nIn = trainingIter.inputColumns()
    val nOut = trainingIter.totalOutcomes()
    val netConf: MultiLayerConfiguration = configureNetwork(confItem, nIn, nOut)
    val net = new MultiLayerNetwork(netConf)
    net.init()
    net.setListeners(new ScoreIterationListener(1))
    logger.info(net.summary())
    net
  }

  private def configureNetwork(confItem: NeuralNetworkConfItem, nIn: Int, nOut: Int): MultiLayerConfiguration = {
    val nnConf = new NeuralNetConfiguration.Builder()
      .seed(confItem.seed)
      .l2(0.0001)
      .weightInit(confItem.weightInit)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .updater(new Adam(confItem.learningRate))
      .list()

    addLayers(nnConf, confItem, nIn, 0)

    nnConf.layer(new RnnOutputLayer.Builder(confItem.lossFunction).activation(confItem.activationRNN)
      .nIn(confItem.layerWidth).nOut(nOut)
      .dropOut(0.8)
      .build())
      .backpropType(confItem.tbpttType)
      .tBPTTForwardLength(confItem.tbpttLength)
      .tBPTTBackwardLength(confItem.tbpttLength)
      .build()
  }

  @tailrec
  private def addLayers(conf: NeuralNetConfiguration.ListBuilder, confItem: NeuralNetworkConfItem,
                        nIn: Int, idx: Int): Unit = {
    if (idx < confItem.layerCount) {
      conf.layer(new LSTM.Builder().nIn(nIn).nOut(confItem.layerWidth)
        .activation(confItem.activationLSTM).build())
      addLayers(conf, confItem, confItem.layerWidth, idx + 1)
    }
  }

  /** Get data from training data file.
   *
   * @param twitterUsername Twitter username to whom the file belongs.
   * @param isTextFile True if it is the text file.
   *                   False if it is the csv file.
   * @return Data readed.
   * @throws FileNotFoundException if system cannot find the file specified.
   */
  def getData(twitterUsername: String, isTextFile: Boolean): Array[String] = {
    try {
      val dataSetFileName: String = "./data(generated)/" + twitterUsername + getFormat(isTextFile)
      val data = IOUtils.toString(new FileInputStream(dataSetFileName), "UTF-8")
      data.split(getSplitSymbol)
    }
    catch {
      case exception: FileNotFoundException =>
        logger.info(exception.getMessage)
        throw exception
    }
  }
  private def getFormat(isTextFile: Boolean): String = {
    if (isTextFile) { ".txt" } else { ".csv" }
  }

  /**
   * @return Percentage value for total data.
   */
  def getTotalPercentage: Int = {
    totalPercentage
  }

  /**
   * @return Training data percentage value.
   */
  def getTrainingPercentage: Int = {
    trainingPercentage
  }

  /**
   * @return Text data percentage value.
   */
  def getTestPercentage: Int = {
    testPercentage
  }

  /**
   * @return line split symbol string
   */
  def getSplitSymbol: String = {
    splitSymbol
  }



  /** Get training data.
   *
   * @param splitData Data to split.
   * @param splitSize Split size.
   * @return Data splitted.
   */
  def getTrainingData(splitData: Array[String], splitSize: Int): Array[String] = {
    checkNotEmptySeq(splitData)
    checkNotNegativeInt(splitSize)

    val idx = 0
    val arrayToReturn: Array[String] = new Array[String](splitSize)
    addTrainingElement(arrayToReturn, idx, splitData, splitSize)
    arrayToReturn
  }

  @tailrec
  private def addTrainingElement(arrayToReturn: Array[String], idx: Int, splitData: Array[String], splitSize: Int)
  : Array[String] = {
    if (idx < splitSize) {
      arrayToReturn(idx) = splitData(idx)
      addTrainingElement(arrayToReturn, idx + 1, splitData, splitSize)
    }
    else {
      arrayToReturn
    }
  }


  /** Get test data.
   *
   * @param splitData Data to split.
   * @param splitSize Size of the split.
   * @return Split data.
   */
  def getTestData(splitData: Array[String], splitSize: Int) : Array[String] = {
    checkNotEmptySeq(splitData)
    checkNotNegativeInt(splitSize)

    val idx = 0
    val arrayToReturn: Array[String] = new Array[String](splitData.length - splitSize)
    addTestElement(arrayToReturn, idx, splitData, splitSize)
    arrayToReturn
  }

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



  /** Prepare text to sample characters from a neural network model.
   *
   * @param nCharactersToSample. Number of characters to sample.
   * @return The generated text.
   */
  def prepareText(twitterUsername: String, nCharactersToSample: Int): String = {
    checkNotNegativeInt(nCharactersToSample)
    val initializationString: String = getNoInitializationString
    val rng = new Random()
    val miniBatchSize = getProperties.getProperty("trainingMiniBatchSize").toInt
    val exampleLength = getProperties.getProperty("trainingExampleLength").toInt
    val iter: CharacterGeneratorIterator = getCharacterIterator(twitterUsername, miniBatchSize, exampleLength)
    val postNCharactersToSample = nCharactersToSample
    val textNN = loadNetwork(neuralNetworkFolderPath + twitterUsername + "Text.zip")
    sampleCharactersFromNetwork(initializationString, textNN, iter,
      rng, postNCharactersToSample)
  }

  private def getCharacterIterator(twitterUsername: String, miniBatchSize: Int, exampleLength: Int)
  : CharacterGeneratorIterator = {
    val data = IOUtils.toString(new FileInputStream("./data(generated)/" + twitterUsername + ".txt"), "UTF-8")
    new CharacterGeneratorIterator(miniBatchSize, exampleLength, data.split("\n"))
  }

  /** Load neural network from given path.
   *
   * @param name. Neural network path.
   * @return MultiLayerNetwork object.
   * @throws NotExistingFileException if file does not exist.
   */
  def loadNetwork(name: String): MultiLayerNetwork = {
    val textNetworkLocation = new File(name)
    if (!textNetworkLocation.exists()) {
      throw NotExistingFileException("File " + name + " does not exist")
    }
    MultiLayerNetwork.load(textNetworkLocation, false)
  }


  /** Generate action.
   *
   * @param followedPostActionsCount Count of followed post actions.
   * @param maxFollowedPostActions Number of maximum followed post actions.
   * @param tweets Last tweet as a sequence
   * @return TypeAndDate object with the generated action type and execution date.
   */
  def generateNextAction(twitterUsername: String, followedPostActionsCount: Int, maxFollowedPostActions: Int,
                         tweets: Seq[StatusImpl]): NNActionItem = {
    checkNotNegativeInt(followedPostActionsCount)
    checkNotNegativeInt(maxFollowedPostActions)
    checkNotEmptySeq(tweets)
    val inputArray = getNNInputArrayFromTweets(tweets)

    generateNextTypeAndDateAction(twitterUsername, followedPostActionsCount, maxFollowedPostActions, inputArray)
  }


  private def generateNextTypeAndDateAction(twitterUsername: String, followedPostActionsCount: Int,
  maxFollowedPostActions: Int, inputArray: INDArray): NNActionItem = {
    try {
      val typeAndDateNN = loadNetwork(neuralNetworkFolderPath + twitterUsername + "Action.zip")
      typeAndDateNN.rnnClearPreviousState()

      val output = typeAndDateNN.output(inputArray)

      val day = getOutputDay(output.getDouble(0.toLong))
      val hour = getOutputHour(output.getDouble(1.toLong))
      val action = getActionValue(getOutputAction(output.getDouble(2.toLong)))
      buildNNActionItemFromDayHourAndAction(day, hour, action, followedPostActionsCount, maxFollowedPostActions)
    }
    catch {
      case exception: NotExistingFileException => logger.error("Neural network file does not exist.\n" + exception.msg)
        System.exit(1)
        buildNNActionItemFromDayHourAndAction(1, 1, 1, followedPostActionsCount, maxFollowedPostActions)
    }
  }

  private def getActionValue(value : Long): Int = {
    if (value < 1) {
      1
    }
    else { value.toInt }
  }

  /** Get neural network day output as an integer value.
   *
   * @param outputDay Neural network output day value.
   * @return Value of neural network output rounded up as an integer.
   */
  def getOutputDay(outputDay: Double): Int = {
    checkNotNegativeLong(outputDay.toLong)
    (outputDay*7).toInt
  }

  /** Get neural network hour output as an integer.
   *
   * @param outputHour Neural network output hour value.
   * @return Value of neural network output hour rounded up as an integer.
   */
  def getOutputHour(outputHour: Double): Int = {
    checkNotNegativeLong(outputHour.toLong)
    (outputHour*23).toInt
  }

  /** Function that compose action value from neural network output.
   *
   * @param outputAction Neural network output action value.
   * @return Value of neural network output action rounded up as an integer.
   */
  def getOutputAction(outputAction: Double): Int = {
    try {
      checkNotNegativeLong(outputAction.toLong)
      (outputAction*3).toInt
    }
    catch {
      case exception: WrongParamValueException =>
        logger.error(exception.msg)
        1
    }
  }


  private def getNNInputArrayFromTweets(tweets: Seq[StatusImpl]): INDArray = {
    val numberOfInputs = tweets.length
    val numberOfInputElements = 3
    val numberOfTimeSteps = 1
    val inputArray: INDArray = Nd4j.create(Array[Long](numberOfInputs, numberOfInputElements, numberOfTimeSteps),
      'f')
    val idx: Int = 0
    getNNInputArrayFromTweetsLoop(tweets, inputArray, idx)
  }

  @tailrec
  private def getNNInputArrayFromTweetsLoop(tweets: Seq[StatusImpl], inputArray: INDArray, idx: Int): INDArray = {
    if (idx < tweets.length) {
      addInputToArray(inputArray, tweets(idx), idx)
      getNNInputArrayFromTweetsLoop(tweets, inputArray, idx + 1)
    }
    else { inputArray }
  }

  private def addInputToArray(inputArray: INDArray, status: StatusImpl, idx: Long): Unit = {
    val nnActionItem: NNActionItem = statusToNNActionItem(status)

    inputArray.putScalar(Array[Long](idx, 0, 0), nnActionItem.day.get.toLong / 7.0)
    inputArray.putScalar(Array[Long](idx, 1, 0), nnActionItem.hour.get.toLong / 23.0)
    inputArray.putScalar(Array[Long](idx, 2, 0), nnActionItem.commandTrait.get.value / 3.0)
  }

  private def getNoInitializationString : String = {
    ""
  }
}
