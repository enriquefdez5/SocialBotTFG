package rnn

import java.io.FileInputStream
import java.util
import java.util.Properties

import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.conf.layers.{LSTM, RnnOutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.RmsProp
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import utilities.FileManagement.FileReaderUtil

import scala.annotation.tailrec


object Rnn2 extends Logging{

  // Read properties file
  val properties: Properties = new Properties()
  properties.load(new FileInputStream("src/main/resources/config.properties"))

  // RNN dimensions // Exportable value
  val HIDDEN_LAYER_WIDTH: Int = properties.getProperty("hiddenLayerWidth").toInt // It is a constant value I can export
  // to a file
  val HIDDEN_LAYER_CONT: Int = properties.getProperty("hiddenLayerCont").toInt
  val epochs: Int = properties.getProperty("trainingNumberOfEpochs").toInt
  // Conf parameters
  val confItem: NeuralNetworkConfItem = NeuralNetworkConfItem(properties.getProperty("trainingSeed").toInt,
    properties.getProperty("trainingBiasInit").toInt, properties.getProperty("trainingMiniBatch").toBoolean,
    properties.getProperty("trainingLearningRate").toDouble, WeightInit.valueOf(properties.getProperty
    ("trainingWeightInit")))

//  private val LEARNSTRING = "*Der Cottbuser Postkutscher putzt den Cottbuser Postkutschkasten.".toCharArray
  private val LEARNSTRING_CHARS_LIST = new util.ArrayList[Character]

  def main(args: Array[String]): Unit = {

    // Read data from file
    val data = FileReaderUtil.readDataFromAFile()
    val splittedData: Array[String] = data.toString.split("%")
    splittedData.foreach { learnString: String =>

      // create a dedicated list of possible chars in LEARNSTRING_CHARS_LIST
      LEARNSTRING_CHARS_LIST.addAll(util.Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'ñ', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '.', ',', '(', ')', '_', '-', '!',
      '¡',
        '¿', '?', ' ', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'á', 'é', 'í', 'ó', 'ú', ':', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'Ñ', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
        'X', 'Y', 'Z'))

      // some common parameters
      val builder: NeuralNetConfiguration.Builder = new NeuralNetConfiguration.Builder()
      builder.seed(confItem.seed)
      builder.biasInit(confItem.biasInit)
      builder.miniBatch(confItem.miniBatch)
      builder.updater(new RmsProp(confItem.learningRate))
      builder.weightInit(confItem.weightInit)

      val listBuilder: NeuralNetConfiguration.ListBuilder = builder.list()

      //    // first difference, for rnns we need to use LSTM.Builder
      configureHiddenLayer(0, LEARNSTRING_CHARS_LIST.size(), listBuilder)

      // we need to use RnnOutputLayer for our RNN
      val outputLayerBuilder: RnnOutputLayer.Builder = new RnnOutputLayer.Builder(LossFunction.MCXENT)
      // softmax normalizes the output neurons, the sum of all outputs is 1
      // this is required for our sampleFromDistribution-function
      outputLayerBuilder.activation(Activation.SOFTMAX)
      outputLayerBuilder.nIn(HIDDEN_LAYER_WIDTH)
      outputLayerBuilder.nOut(LEARNSTRING_CHARS_LIST.size())
      listBuilder.layer(HIDDEN_LAYER_CONT, outputLayerBuilder.build())

      // create network
      val conf: MultiLayerConfiguration = listBuilder.build()
      val net = new MultiLayerNetwork(conf)
      net.init()
      net.setListeners(new ScoreIterationListener(1))

      /*
     * CREATE OUR TRAINING DATA
     */
      // create input and output arrays: SAMPLE_INDEX, INPUT_NEURON,
      // SEQUENCE_POSITION
      val input: INDArray = Nd4j.zeros(1, LEARNSTRING_CHARS_LIST.size(), learnString.length)
      val labels: INDArray = Nd4j.zeros(1, LEARNSTRING_CHARS_LIST.size(), learnString.length)
      // loop through our sample-sentence
      var samplePos: Int = 0
      learnString.foreach { currentChar: Char =>
        // small hack: when currentChar is the last, take the first char as
        // nextChar - not really required. Added to this hack by adding a starter first character.
        val nextChar = learnString.charAt((samplePos + 1) % learnString.length)
        // input neuron for current-char is 1 at "samplePos"
        input.putScalar(Array(0, LEARNSTRING_CHARS_LIST.indexOf(currentChar), samplePos), 1)
        // output neuron for next-char is 1 at "samplePos"
        labels.putScalar(Array(0, LEARNSTRING_CHARS_LIST.indexOf(nextChar), samplePos), 1)
        samplePos += 1
      }
      val trainingData: DataSet = new DataSet(input, labels)

      // some epochs
      // training and guessing characters
      epochTraining(0, net, trainingData, LEARNSTRING_CHARS_LIST,
        learnString.toCharArray)
    }
  }

  @tailrec
  def epochTraining(index: Int, net: MultiLayerNetwork, trainingData: DataSet,
                    learnStringCharsList: util.List[Character],
                    learnString: Array[Char]): Unit = {
    if (index < epochs) {
      logger.debug(s"Epoch $index ")
      // train the data
      net.fit(trainingData)
      // clear current stance from the last example
      net.rnnClearPreviousState()
      // put the first character into the rnn as an initialisation
      val testInit = Nd4j.zeros(1, learnStringCharsList.size, 1)
      testInit.putScalar(learnStringCharsList.indexOf(learnString(0)), 1)
      // run one step -> IMPORTANT: rnnTimeStep() must be called, not output()
      // the output shows what the net thinks what should come next
      val output: INDArray = net.rnnTimeStep(testInit)
      guessCharacters(0, output, learnStringCharsList, net, learnString)
      logger.debug("\n")
      epochTraining(index+1, net, trainingData, learnStringCharsList, learnString)
    }
  }


  @tailrec
  def guessCharacters(idx: Int, output: INDArray, learnStringCharsList: util.List[Character], net: MultiLayerNetwork,
                      learnString: Array[Char]): Unit = {
    if (idx < learnString.length) {
      // first process the last output of the network to a concrete
      // neuron, the neuron with the highest output has the highest
      // chance to get chosen

      val sampledCharacterIdx = Nd4j.getExecutioner.exec(new IMax(output, 1)).getInt(0)

      // print the chosen output
      logger.debug(learnStringCharsList.get(sampledCharacterIdx))

      // use the last output as input
      val nextInput = Nd4j.zeros(1, learnStringCharsList.size, 1)
      nextInput.putScalar(sampledCharacterIdx, 1)
      guessCharacters(idx+1, net.rnnTimeStep(nextInput), learnStringCharsList, net, learnString)
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
}
