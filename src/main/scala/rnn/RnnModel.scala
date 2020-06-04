package rnn

import java.io.{File, FileInputStream}
import java.util
import java.util.Properties

import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.nn.conf.layers.{ LSTM, RnnOutputLayer}
import org.deeplearning4j.nn.conf.{BackpropType, MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

import scala.annotation.tailrec

object RnnModel extends Logging {
  // Read properties file
  val properties: Properties = new Properties()
  properties.load(new FileInputStream("src/main/resources/config.properties"))

  // RNN dimensions // Exportable value
  val HIDDEN_LAYER_WIDTH: Int = properties.getProperty("hiddenLayerWidth").toInt // It is a constant value I can export
  // to a file
  val HIDDEN_LAYER_CONT: Int = properties.getProperty("hiddenLayerCont").toInt
  // Conf parameters
  val confItem: NeuralNetworkConfItem = NeuralNetworkConfItem(
    properties.getProperty("trainingSeed").toInt,
    properties.getProperty("trainingBiasInit").toInt,
    properties.getProperty("trainingMiniBatch").toBoolean,
    properties.getProperty("trainingLearningRate").toDouble,
    WeightInit.valueOf(properties.getProperty("trainingWeightInit")),
    properties.getProperty("trainingL2").toDouble,
    properties.getProperty("trainingTbpttLength").toInt
  )
  // Other parameters for layer and training conf
  val epochs: Int = properties.getProperty("trainingNumberOfEpochs").toInt
  val batchSize: Int = properties.getProperty("trainingBatchSize").toInt
  val generateSamplesEveryNMinibatches: Int = properties.getProperty("trainingGenerateSamplesEveryNMinibatches").toInt


  // create a dedicated list of possible chars in LEARNSTRING_CHARS_LIST
  private val LEARNSTRING_CHARS_LIST = util.Arrays.asList('$', '&', '#', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
    'j', 'k', 'l', 'm', 'n', 'ñ', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '.', ',', '(', ')', '_',
    '-', '!', '¡', '¿', '?', ' ', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'á', 'é', 'í', 'ó', 'ú', ':', 'A',
    'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'Ñ', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
    'X', 'Y', 'Z')

  def buildNetwork(splittedData: Array[String]): MultiLayerNetwork = {
    // some common parameters to configure the network
    val builder: NeuralNetConfiguration.Builder = new NeuralNetConfiguration.Builder()
    builder.seed(confItem.seed)
    builder.biasInit(confItem.biasInit)
    builder.miniBatch(confItem.miniBatch)
    builder.updater(new Adam(confItem.learningRate)) // RmsProp instead of Adam is another good option
    builder.weightInit(confItem.weightInit)
    builder.l2(confItem.l2)

    val listBuilder: NeuralNetConfiguration.ListBuilder = builder.list()
    // first difference, for rnns we need to use LSTM.Builder
    configureHiddenLayer(0, LEARNSTRING_CHARS_LIST.size(), listBuilder)

    // we need to use RnnOutputLayer for our RNN
    val outputLayerBuilder: RnnOutputLayer.Builder = new RnnOutputLayer.Builder(LossFunction.MCXENT)
    // softmax normalizes the output neurons, the sum of all outputs is 1
    // this is required for our sampleFromDistribution-function
    outputLayerBuilder.activation(Activation.SOFTMAX)
    outputLayerBuilder.nIn(HIDDEN_LAYER_WIDTH)
    outputLayerBuilder.nOut(LEARNSTRING_CHARS_LIST.size())
    listBuilder.layer(HIDDEN_LAYER_CONT, outputLayerBuilder.build())
      .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(confItem.tbpttLength)
      .tBPTTBackwardLength(confItem.tbpttLength)

    // create network
    val conf: MultiLayerConfiguration = listBuilder.build()

    val net = new MultiLayerNetwork(conf)
    net.init()
    net.setListeners(new ScoreIterationListener(1))
    training(splittedData, net, 0, 1)
    net

  }

  @tailrec
  def training(splittedData: Array[String], net: MultiLayerNetwork, index: Int, nMiniBatch: Int): Unit = {
    if (index < splittedData.length){
      val learnString = splittedData(index)
      // testing with batch stuff
//      val learnString = getLearnStringWithBatchSize(splittedData, "", index, nMiniBatch)
      /*
      * CREATE OUR TRAINING DATA
      */
      // create input and output arrays: SAMPLE_INDEX, INPUT_NEURON,
      // SEQUENCE_POSITION
      val input: INDArray = Nd4j.zeros(1, LEARNSTRING_CHARS_LIST.size(), learnString.length)
      val labels: INDArray = Nd4j.zeros(1, LEARNSTRING_CHARS_LIST.size(), learnString.length)
      // loop through our sample-sentence
      val trainingData: DataSet = createTrainingData(0, learnString, input, labels)
      // some epochs
      // training and guessing characters
      logger.debug(s"La cadena a entrenar es: $learnString")
      epochTraining(0, net, trainingData, LEARNSTRING_CHARS_LIST,
        learnString.toCharArray, nMiniBatch)
      training(splittedData, net, index + batchSize, nMiniBatch + 1)
    }
  }

  @tailrec
  def getLearnStringWithBatchSize(splittedData: Array[String], learnString: String, index: Int, nMiniBatch: Int): String
  = {
    if (index < (nMiniBatch * batchSize) && index < splittedData.length) {
      getLearnStringWithBatchSize(splittedData, learnString.concat(splittedData(index)), index + 1, nMiniBatch)
    }
    else {
      learnString
    }

  }

  @tailrec
  def createTrainingData(samplePos: Int, learnString: String, input: INDArray, labels: INDArray): DataSet = {

    if (samplePos < learnString.length) {
      val currentChar: Char = learnString(samplePos)

      // small hack: when currentChar is the last, take the first char as
      // nextChar - not really required. Added to this hack by adding a starter first character.
      val nextChar = learnString.charAt((samplePos + 1) % learnString.length)
      // input neuron for current-char is 1 at "samplePos"
      input.putScalar(Array(0, LEARNSTRING_CHARS_LIST.indexOf(currentChar), samplePos), 1)
      // output neuron for next-char is 1 at "samplePos"
      labels.putScalar(Array(0, LEARNSTRING_CHARS_LIST.indexOf(nextChar), samplePos), 1)

      createTrainingData(samplePos + 1, learnString, input, labels)
    }
    else {
      new DataSet(input, labels)
    }
  }

  @tailrec
  def epochTraining(index: Int, net: MultiLayerNetwork, trainingData: DataSet,
                    learnStringCharsList: util.List[Char],
                    learnString: Array[Char], nMiniBatch: Int): Unit = {
    if (index < epochs) {
      logger.debug(s"Epoch $index ")
      // train the data
      net.fit(trainingData)

      // Generate sample each nMiniBatch times
      if (nMiniBatch % generateSamplesEveryNMinibatches == 0) {
        // clear current stance from the last example
        net.rnnClearPreviousState()
        // put the first character into the rnn as an initialisation
        val testInit = Nd4j.zeros(1, learnStringCharsList.size, 1)
        testInit.putScalar(learnStringCharsList.indexOf(learnString(0)), 1)

        //    // run one step -> IMPORTANT: rnnTimeStep() must be called, not output()
        //    // the output shows what the net thinks what should come next
        val output: INDArray = net.rnnTimeStep(testInit)
        guessCharacters(0, output, learnStringCharsList, net, learnString, stringGenerated = "&")
      }
      epochTraining(index+1, net, trainingData, learnStringCharsList, learnString, nMiniBatch)
    }
  }

  @tailrec
  def guessCharacters(idx: Int, output: INDArray, learnStringCharsList: util.List[Char], net: MultiLayerNetwork,
                      learnString: Array[Char], stringGenerated: String): Unit = {
      if (idx < 180) { // 180 = sentence maxLength
        // first process the last output of the network to a concrete
        // neuron, the neuron with the highest output has the highest
        // chance to get chosen
        val sampledCharacterIdx = Nd4j.getExecutioner.exec(new IMax(output, 1)).getInt(0)
        // print the chosen output
        logger.debug(learnStringCharsList.get(sampledCharacterIdx).toString)
        // use the last output as input
        val nextInput = Nd4j.zeros(1, learnStringCharsList.size, 1)
        nextInput.putScalar(sampledCharacterIdx, 1)
        guessCharacters(idx+1, net.rnnTimeStep(nextInput), learnStringCharsList, net, learnString, stringGenerated.+(learnStringCharsList.get(sampledCharacterIdx).toString))
      }
      else {
        logger.debug(s"String generated is:  $stringGenerated \n")
      }
  }

  @tailrec
  def configureHiddenLayer(i: Int, learnStringCharsSize: Int, listBuilder: NeuralNetConfiguration.ListBuilder ): Unit
  = {
    if (i < HIDDEN_LAYER_CONT) {
      val hiddenLayerBuilder = new LSTM.Builder()
      if (i == 0) {
        hiddenLayerBuilder.nIn(learnStringCharsSize)
        logger.debug(s"Size learnStringCharsSize i == 0 $learnStringCharsSize")
      }
      else {
        hiddenLayerBuilder.nIn(HIDDEN_LAYER_WIDTH)
        logger.debug(s"Size else $HIDDEN_LAYER_WIDTH")
      }
      hiddenLayerBuilder.nOut(HIDDEN_LAYER_WIDTH)

      // adopted activation function from LSTMCharModellingExample
      // seems to work well with RNNs
      hiddenLayerBuilder.activation(Activation.TANH)
      listBuilder.layer(i, hiddenLayerBuilder.build())

      configureHiddenLayer(i+1, learnStringCharsSize, listBuilder)
    }
  }

  def saveNet(net: MultiLayerNetwork): Unit = {
    val locationToSave = new File("LSTMRnnNet30epochs1batchSize.zip")
    val saveUpdater = true
    net.save(locationToSave, saveUpdater)
  }
}
