package utilities.neuralNetworks

import java.io.{File, FileInputStream}
import java.util.Random

import model.TypeAndDate.{buildTypeAndDateFromDayHourAndAction, postToTypeAndDate}
import model.exceptions.NotExistingFileException
import model.{StatusImpl, TypeAndDate}
import neuralNetworks.rnnCharacterGenerator.CharacterGeneratorIterator
import neuralNetworks.rnnCharacterGenerator.MainNNCharacterGenerator.sampleCharactersFromNetwork
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import utilities.properties.PropertiesReaderUtilTrait
import utilities.validations.ValidationsUtilTrait

import scala.annotation.tailrec

trait NeuralNetworkUtilTraitTraitTrait extends Logging with PropertiesReaderUtilTrait with ValidationsUtilTrait{


  /**
   * Function that loads neural network for generating text.
   * @param nCharactersToSample. Number of characters to generate
   * @return the generated text as String
   */
  def prepareText(nCharactersToSample: Int): String = {
    checkNotNegativeInt(nCharactersToSample)
    val initializationString: String = getNoInitializationString
    val rng = new Random()
    val miniBatchSize = getProperties.getProperty("trainingMiniBatchSize").toInt
    val exampleLength = getProperties.getProperty("trainingExampleLength").toInt
    val iter: CharacterGeneratorIterator = getCharacterIterator(miniBatchSize, exampleLength, rng)
    val postNCharactersToSample = nCharactersToSample
    val textNN = loadNetwork(getProperties.getProperty("textNNPath"))
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
  private def getCharacterIterator(miniBatchSize: Int, exampleLength: Int, rng: Random): CharacterGeneratorIterator = {
    val data = IOUtils.toString(new FileInputStream(getProperties.getProperty("dataSetFileName")), "UTF-8")
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
  def generateNextAction(followedPostActionsCount: Int, maxFollowedPostActions: Int, tweets: Seq[StatusImpl])
  : TypeAndDate = {
    // Get last tweet
    logger.debug("-------------------------------")
    logger.debug("followedPostActionsCount: ")
    logger.debug(followedPostActionsCount.toString)
    logger.debug("maxFollowedPostActions: ")
    logger.debug(maxFollowedPostActions.toString)
    logger.debug("-------------------------------")
    checkNotNegativeInt(followedPostActionsCount)
    checkNotNegativeInt(maxFollowedPostActions)
    checkNotEmptySeq(tweets)
    val inputArray = getNNInputArrayFromTweets(tweets)

    generateNextTypeAndDateAction(followedPostActionsCount, maxFollowedPostActions, inputArray)

  }


  /**
   * Function that loads the neural network that generates a TypeAndDate object from an INDArray.
   * @param inputArray. INDArray built with last 5 tweets that will be passed to the neural network as a parameter.
   * @return a TypeAndDate object built with the output from the neural network.
   */
  private def generateNextTypeAndDateAction(followedPostActionsCount: Int, maxFollowedPostActions: Int,
                                            inputArray: INDArray): TypeAndDate = {
    // Load other neural networks for date generation
    val typeAndDateNN = loadNetwork("./models/typeAndDateNN.zip")
    typeAndDateNN.rnnClearPreviousState()

    // Generate output
    val output = typeAndDateNN.output(inputArray)

    // Compose typeAndDate object with output
    val day = getOutputDay(output.getDouble(0.toLong))
    val hour = getOutputHour(output.getDouble(1.toLong))
    val action = getActionValue(getOutputAction(output.getDouble(2.toLong)))
    buildTypeAndDateFromDayHourAndAction(day, hour, action, followedPostActionsCount, maxFollowedPostActions)
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
    (outputDay*7).toInt
  }

  /**
   * Function that compose hour value from neural network output.
   * @param outputHour. Double value from neural network output.
   * @return an Integer value that represents the hour of the day in which the action will be executed.
   */
  def getOutputHour(outputHour: Double): Int = {
    checkNotNegativeLong(outputHour.toLong)
    (outputHour*23).toInt
  }

  /**
   * Function that compose action value from neural network output.
   * @param outputAction. Double value from neural network output.
   * @return an Integer value that represents the chosen action that will be executed.
   */
  def getOutputAction(outputAction: Double): Int = {
    checkNotNegativeLong(outputAction.toLong)
    (outputAction*3).toInt
  }


  /**
   * Function that build an INDArray with the last 5 tweets posted by the user.
   * @param tweets. Last 5 tweets posted by the user.
   * @return an INDArray built with params tweets.
   */
  private def getNNInputArrayFromTweets(tweets: Seq[StatusImpl]): INDArray = {

    val numberOfInputs = 5
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
    val typeAndDate: TypeAndDate = postToTypeAndDate(post)

    logger.debug("-------------------------------")
    logger.debug(typeAndDate.dayOfWeek.toString)
    logger.debug((typeAndDate.dayOfWeek.toLong / 7.0).toString)
    logger.debug(typeAndDate.hourOfDay.toString)
    logger.debug((typeAndDate.hourOfDay.toLong / 23.0).toString)
    logger.debug(typeAndDate.action.toString)
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
}