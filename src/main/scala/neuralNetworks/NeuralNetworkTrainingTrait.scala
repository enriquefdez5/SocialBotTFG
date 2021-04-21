package neuralNetworks

import java.io.{File, FileInputStream}
import java.util.Random
import java.util.concurrent.TimeUnit

import model.NNActionItem.{buildTypeAndDateFromDayHourAndAction, postToTypeAndDate}
import model.{NNActionItem, StatusImpl}
import model.exceptions.{NotExistingFileException, WrongParamValueException}
import neuralNetworks.rnnCharacterGenerator.CharacterGeneratorIterator
import neuralNetworks.rnnCharacterGenerator.MainNNCharacterGenerator.sampleCharactersFromNetwork
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator
import org.deeplearning4j.earlystopping.termination.{MaxEpochsTerminationCondition, MaxTimeIterationTerminationCondition}
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j
import utilities.properties.PropertiesReaderUtilTrait
import utilities.validations.ValidationsUtilTrait

import scala.annotation.tailrec

trait NeuralNetworkTrainingTrait extends Logging with ValidationsUtilTrait with PropertiesReaderUtilTrait {


  val splitSymbol: String = "\n"
//  val splitSymbol: String = ","
  val totalPercentage: Int = 100
  val trainingPercentage: Int = 80
  val testPercentage: Int = totalPercentage-trainingPercentage


  def getTotalPercentage: Int = {
    totalPercentage
  }

  def getTrainingPercentage: Int = {
    trainingPercentage
  }

  def getTestPercentage: Int = {
    testPercentage
  }

  def getSplitSymbol: String = {
    splitSymbol
  }

  def getEsConf(maxEpochNumber: Int, maxTimeAmount: Int, testIter: DataSetIterator, saver: LocalFileModelSaver)
  : EarlyStoppingConfiguration[MultiLayerNetwork] = {
    new EarlyStoppingConfiguration.Builder()
      .epochTerminationConditions(new MaxEpochsTerminationCondition(maxEpochNumber))
      .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(maxTimeAmount, TimeUnit.MINUTES))
      .scoreCalculator(new DataSetLossCalculator(testIter, true))
      .evaluateEveryNEpochs(1)
      .modelSaver(new LocalFileModelSaver("./models/"))
      .build()
  }

  def getSaver(directory: String): LocalFileModelSaver = {
    val dirFile: File = new File(directory)
    dirFile.mkdir()
    new LocalFileModelSaver(directory)
  }



  def saveNetwork(net: MultiLayerNetwork, path: String): Unit = {
    //    val locationToSave = new File(getProperties.getProperty("textNNPath"))
    net.save(new File(path), true)
  }

  /**
   * Private function that splits dataset into training data and testing data.
   * @param splitData, Array[String]. Read dataset to be split.
   * @param splitSize, Int. Size of the split.
   * @return Array[String]. Split string with the given size.
   */
  def getTrainingData(splitData: Array[String], splitSize: Int): Array[String] = {
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


  /**
   * Private function that returns the test data.
   * @param splitData, Array[String]. Read dataset to be split.
   * @param splitSize, Int.Size of the split.
   * @return Array[String]. Split string with the given size.
   */
  def getTestData(splitData: Array[String], splitSize: Int) : Array[String] = {
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
   * Function that loads neural network for generating text.
   * @param nCharactersToSample. Number of characters to generate
   * @return the generated text as String
   */
  def prepareText(twitterUsername: String, nCharactersToSample: Int): String = {
    checkNotNegativeInt(nCharactersToSample)
    val initializationString: String = getNoInitializationString
    val rng = new Random()
    val miniBatchSize = getProperties.getProperty("trainingMiniBatchSize").toInt
    val exampleLength = getProperties.getProperty("trainingExampleLength").toInt
    val iter: CharacterGeneratorIterator = getCharacterIterator(twitterUsername, miniBatchSize, exampleLength, rng)
    val postNCharactersToSample = nCharactersToSample
    val textNN = loadNetwork("./models/" + twitterUsername + "Text.zip")
    sampleCharactersFromNetwork(initializationString, textNN, iter,
      rng, postNCharactersToSample)
  }

  /**
   * Function that creates a character iterator needed to sample characters.
   * @param miniBatchSize. Int value representing the mini batch size.
   * @param exampleLength. Int value representing the length of each example to be loaded.
   * @param rng. Random object used to set a seed value.
   * @return CharacterIterator object needed to sample characters.
   */
  private def getCharacterIterator(twitterUsername: String, miniBatchSize: Int, exampleLength: Int, rng: Random)
  : CharacterGeneratorIterator = {
    val data = IOUtils.toString(new FileInputStream("./data(generated)/" + twitterUsername + ".txt"), "UTF-8")
    new CharacterGeneratorIterator(miniBatchSize, exampleLength, rng, data.split("\n"))
  }

  /**
   * Function that loads a neural network from the app files.
   * @param name. String that is the neural network file name to load.
   * @return MultiLayerNetwork object that is the loaded neural network.
   */
  def loadNetwork(name: String): MultiLayerNetwork = {
    val textNetworkLocation = new File(name)
    if (!textNetworkLocation.exists()) {
      throw NotExistingFileException("File " + name + " does not exist")
    }
    MultiLayerNetwork.load(textNetworkLocation, false)
  }


  /**
   * Function that generates next type and date action.
   * @param followedPostActionsCount, Int. Count of followed post actions.
   * @param maxFollowedPostActions, Int. Number of maximum followed post actions.
   * @param tweets, Seq[Post]. Last five tweets gathered from the active user.
   * @return TypeAndDate. TypeAndDate object with the generated action type and execution date.
   */
  def generateNextAction(twitterUsername: String, followedPostActionsCount: Int, maxFollowedPostActions: Int,
                         tweets: Seq[StatusImpl]): NNActionItem = {
    // Get last tweet
    checkNotNegativeInt(followedPostActionsCount)
    checkNotNegativeInt(maxFollowedPostActions)
    checkNotEmptySeq(tweets)
    val inputArray = getNNInputArrayFromTweets(tweets)

    generateNextTypeAndDateAction(twitterUsername, followedPostActionsCount, maxFollowedPostActions, inputArray)
  }


  /**
   * Function that loads the neural network that generates a TypeAndDate object from an INDArray.
   * @param inputArray. INDArray built with last 5 tweets that will be passed to the neural network as a parameter.
   * @return a TypeAndDate object built with the output from the neural network.
   */
  private def generateNextTypeAndDateAction(twitterUsername: String, followedPostActionsCount: Int,
  maxFollowedPostActions: Int, inputArray: INDArray): NNActionItem = {
    // Load other neural networks for date generation
    try {
      val typeAndDateNN = loadNetwork("./models/" + twitterUsername + "Action.zip")
      typeAndDateNN.rnnClearPreviousState()

      // Generate output
      val output = typeAndDateNN.output(inputArray)

      // Compose typeAndDate object with output
      val day = getOutputDay(output.getDouble(0.toLong))
      val hour = getOutputHour(output.getDouble(1.toLong))
      val action = getActionValue(getOutputAction(output.getDouble(2.toLong)))
      buildTypeAndDateFromDayHourAndAction(day, hour, action, followedPostActionsCount, maxFollowedPostActions)
    }
    catch {
      case exception: NotExistingFileException => logger.debug(exception.msg)
        System.exit(1)
        buildTypeAndDateFromDayHourAndAction(1, 1, 1, followedPostActionsCount, maxFollowedPostActions)
    }
  }
  private def getActionValue(value : Long): Int = {
    if (value < 1) {
      1
    }
    else { value.toInt }
  }

  /**
   * Function that compose day value from neural network output.
   * @param outputDay. Double value from neural network output.
   * @return an Integer value that represents the day of the week in which the action will be executed.
   */
  def getOutputDay(outputDay: Double): Int = {
    checkNotNegativeLong(outputDay.toLong)
    logger.debug("Generated output day: " + outputDay.toString)
    (outputDay*7).toInt
  }

  /**
   * Function that compose hour value from neural network output.
   * @param outputHour. Double value from neural network output.
   * @return an Integer value that represents the hour of the day in which the action will be executed.
   */
  def getOutputHour(outputHour: Double): Int = {
    checkNotNegativeLong(outputHour.toLong)
    logger.debug("Generated output hour: " + outputHour.toString)
    (outputHour*23).toInt
  }

  /**
   * Function that compose action value from neural network output.
   * @param outputAction. Double value from neural network output.
   * @return an Integer value that represents the chosen action that will be executed.
   */
  def getOutputAction(outputAction: Double): Int = {
    try {
      checkNotNegativeLong(outputAction.toLong)
      logger.debug("Generated output hour: " + outputAction.toString)
      (outputAction*3).toInt
    }
    catch {
      case exception: WrongParamValueException => {
        logger.info(exception.msg)
        1
      }
    }
  }


  /**
   * Function that build an INDArray with the last 5 tweets posted by the user.
   * @param tweets. Last 5 tweets posted by the user.
   * @return an INDArray built with params tweets.
   */
  private def getNNInputArrayFromTweets(tweets: Seq[StatusImpl]): INDArray = {

    val numberOfInputs = tweets.length
    val numberOfInputElements = 3
    val numberOfTimeSteps = 1
    val inputArray: INDArray = Nd4j.create(Array[Long](numberOfInputs, numberOfInputElements, numberOfTimeSteps),
      'f')
    val idx: Int = 0
    getNNInputArrayFromTweetsLoop(tweets, inputArray, idx)
  }

  /**
   * Tailrec loop function for building an INDArray with a given sequence of tweets.
   * @param tweets, Seq[StatusImpl]. Sequence of post object to iterate over.
   * @param inputArray, INDArray. INDArray to fill with tweets info.
   * @param idx, Int. Index to iterate over tweets sequence.
   * @return INDArray. The INDArray full filled with the info from the sequence of tweets.
   */
  @tailrec
  private def getNNInputArrayFromTweetsLoop(tweets: Seq[StatusImpl], inputArray: INDArray, idx: Int): INDArray = {
    if (idx < tweets.length) {
      addInputToArray(inputArray, tweets(idx), idx)
      getNNInputArrayFromTweetsLoop(tweets, inputArray, idx + 1)
    }
    else { inputArray }
  }


  /**
   * Helper function that adds an element to a INDArray.
   * @param inputArray. INDArray that will add an element.
   * @param post. Tweet to be added to INDArray.
   * @param idx. Idx that indicates the position of the element that will be added to the INDArray.
   */
  private def addInputToArray(inputArray: INDArray, post: StatusImpl, idx: Long): Unit = {
    val typeAndDate: NNActionItem = postToTypeAndDate(post)

    logger.debug("-------------------------------")
    logger.debug("TypeAndDate day of week: " + typeAndDate.dayOfWeek.toString)
    logger.debug((typeAndDate.dayOfWeek.toLong / 7.0).toString)
    logger.debug("TypeAndDate hour of day: " + typeAndDate.hourOfDay.toString)
    logger.debug((typeAndDate.hourOfDay.toLong / 23.0).toString)
    logger.debug("TypeAndDate action: " + typeAndDate.action.toString)
    logger.debug((typeAndDate.action.value / 3.0).toString)
    logger.debug("-------------------------------")

    inputArray.putScalar(Array[Long](idx, 0, 0), typeAndDate.dayOfWeek.toLong / 7.0)
    inputArray.putScalar(Array[Long](idx, 1, 0), typeAndDate.hourOfDay.toLong / 23.0)
    inputArray.putScalar(Array[Long](idx, 2, 0), typeAndDate.action.value / 3.0)
  }

  /**
   * Function that returns an empty string needed to sample characters
   * @return an empty string
   */
  private def getNoInitializationString : String = {
    ""
  }
















  def fitNetwork(maxEpochNumber: Int,
                 maxTimeAmount: Int,
                 trainingIter: DataSetIterator,
                 testIter: DataSetIterator,
                 net: MultiLayerNetwork,
                 saver: LocalFileModelSaver): Unit = {

    val esConf: EarlyStoppingConfiguration[MultiLayerNetwork] = getEsConf(maxEpochNumber,
      maxTimeAmount,
      testIter,
      saver)
    val trainer: EarlyStoppingTrainer = new EarlyStoppingTrainer(esConf, net, trainingIter)
    val result = trainer.fit()

    logger.debug("Termination reason: " + result.getTerminationReason)
    logger.debug("Termination details: " + result.getTerminationDetails)
    logger.debug("Total epochs: " + result.getTotalEpochs)
    logger.debug("Best epoch number: " + result.getBestModelEpoch)
    logger.debug("Score at best epoch: " + result.getBestModelScore)

//    val bestModel: MultiLayerNetwork = result.getBestModel

    //    // Evaluate best model obtained with test data.
    //    evaluateNet(bestModel, testIter)

//    saveNetwork(bestModel, getProperties.getProperty("textNNPath"))
//    bestModel
  }
}
