package utilities.neuralNetworks

import java.io.{File, FileInputStream}
import java.util.Random

import model.TypeAndDate.{buildTypeAndDateFromDayHourAndAction, postToTypeAndDate}
import model.{Post, TypeAndDate}
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import neuralNetworks.rnnCharacterGenerator.CharacterGeneratorIterator
import neuralNetworks.rnnCharacterGenerator.MainNNCharacterGenerator.sampleCharactersFromNetwork
import twitterapi.TwitterService.getLastFiveTweets
import utilities.ConfigRun
import utilities.properties.PropertiesReaderUtil

import scala.annotation.tailrec

trait NeuralNetworkUtils extends Logging with PropertiesReaderUtil {


  /**
   * Function that loads neural network for generating text.
   * @param nCharactersToSample. Number of characters to generate
   * @return the generated text as String
   */
  def prepareText(nCharactersToSample: Int): String = {
    val initializationString: String = getNoInitializationString
    val rng = new Random()
    val miniBatchSize = getProperties.getProperty("trainingMiniBatchSize").toInt
    val exampleLength = getProperties.getProperty("trainingExampleLength").toInt
    val iter: CharacterGeneratorIterator = getCharacterIterator(miniBatchSize, exampleLength, rng)
    val postNCharactersToSample = nCharactersToSample
    val textNN = loadNetwork("./models/bestIbai52epochs.zip")
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
    val data = IOUtils.toString(new FileInputStream("datasetTexto.txt"), "UTF-8")
    new CharacterGeneratorIterator(miniBatchSize, exampleLength, rng, data)
  }

  /**
   * Function that loads a neural network from the app files.
   * @param name. String that is the neural network file name to load.
   * @return MultiLayerNetwork object that is the loaded neural network.
   */
  def loadNetwork(name: String): MultiLayerNetwork = {
    val textNetworkLocation = new File(name)
    MultiLayerNetwork.load(textNetworkLocation, false)
  }

  /**
   * Function that generates next type and date action.
   * @param followedPostActionsCount, Int. Count of followed post actions.
   * @param maxFollowedPostActions, Int. Number of maximum followed post actions.
   * @param conf, ConfigRun. Param needed to connect to the Twitter API.
   * @return TypeAndDate. TypeAndDate object with the generated action type and execution date.
   */
  def generateNextAction(followedPostActionsCount: Int, maxFollowedPostActions: Int, conf: ConfigRun): TypeAndDate = {
    // Get last tweet
    val tweets = getLastFiveTweets(conf, getProperties.getProperty("twitterUsername"))
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
    val action = getOutputAction(output.getDouble(2.toLong))
    buildTypeAndDateFromDayHourAndAction(day, hour, action, followedPostActionsCount, maxFollowedPostActions)
  }

  /**
   * Function that compose day value from neural network output.
   * @param outputDay. Double value from neural network output.
   * @return an Integer value that represents the day of the week in which the action will be executed.
   */
  def getOutputDay(outputDay: Double): Int = {
    logger.debug("Output Day")
    logger.debug(outputDay.toString)
    logger.debug((outputDay*7).toString)
    logger.debug((outputDay*7).toInt.toString)
    (outputDay*7).toInt
  }

  /**
   * Function that compose hour value from neural network output.
   * @param outputHour. Double value from neural network output.
   * @return an Integer value that represents the hour of the day in which the action will be executed.
   */
  def getOutputHour(outputHour: Double): Int = {
    (outputHour*23).toInt
  }

  /**
   * Function that compose action value from neural network output.
   * @param outputAction. Double value from neural network output.
   * @return an Integer value that represents the chosen action that will be executed.
   */
  def getOutputAction(outputAction: Double): Int = {
    (outputAction*3).toInt
  }

  /**
   * Function that build an INDArray with the last 5 tweets posted by the user.
   * @param tweets. Last 5 tweets posted by the user.
   * @return an INDArray built with params tweets.
   */
  private def getNNInputArrayFromTweets(tweets: Seq[Post]): INDArray = {

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
   * @param tweets, Seq[Post]. Sequence of post object to iterate over.
   * @param inputArray, INDArray. INDArray to fill with tweets info.
   * @param idx, Int. Index to iterate over tweets sequence.
   * @return INDArray. The INDArray full filled with the info from the sequence of tweets.
   */
  @tailrec
  private def getNNInputArrayFromTweetsLoop(tweets: Seq[Post], inputArray: INDArray, idx: Int): INDArray = {
    if (idx < tweets.length) {
      addInputToArray(inputArray, tweets(idx), idx)
      getNNInputArrayFromTweetsLoop(tweets, inputArray, idx)
    }
    else { inputArray }
  }

  /**
   * Helper function that adds an element to a INDArray.
   * @param inputArray. INDArray that will add an element.
   * @param post. Tweet to be added to INDArray.
   * @param idx. Idx that indicates the position of the element that will be added to the INDArray.
   */
  private def addInputToArray(inputArray: INDArray, post: Post, idx: Long): Unit = {
    val typeAndDate: TypeAndDate = postToTypeAndDate(post)
    inputArray.putScalar(Array[Long](idx, 0, 0), typeAndDate.dayOfWeek.toLong / 7)
    inputArray.putScalar(Array[Long](idx, 1, 0), typeAndDate.hourOfDay.toLong / 23)
    inputArray.putScalar(Array[Long](idx, 2, 0), typeAndDate.action.value / 3)
  }

  /**
   * Function that returns an empty string needed to sample characters
   * @return an empty string
   */
  private def getNoInitializationString : String = {
    ""
  }
}
