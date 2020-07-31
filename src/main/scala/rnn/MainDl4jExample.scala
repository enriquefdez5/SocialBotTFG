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
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.BooleanIndexing
import org.nd4j.linalg.indexing.conditions.Conditions
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

object MainDl4jExample extends Logging {
  def main(args: Array[String]): Unit = {
    // Read properties file
    val properties: Properties = new Properties()
    properties.load(new FileInputStream("src/main/resources/config.properties"))

    // Use conf item instead

    // Conf parameters
//    val confItem: NeuralNetworkConfItem = NeuralNetworkConfItem(
//      properties.getProperty("trainingSeed").toInt,
//      properties.getProperty("trainingBiasInit").toDouble,
//      properties.getProperty("trainingMiniBatch").toBoolean,
//      properties.getProperty("trainingLearningRate").toDouble,
//      WeightInit.valueOf(properties.getProperty("trainingWeightInit")),
//      properties.getProperty("trainingL2").toDouble,
//      properties.getProperty("trainingTbpttLength").toInt,
//      OptimizationAlgorithm.valueOf(properties.getProperty("trainingOptAlgorithm")),
//      properties.getProperty("trainingRMSDecay").toDouble
//    )

    val lstmLayerSize = 128               // Number of units in each LSTM layer
    val miniBatchSize = 32                // Size of mini batch to use when  training
    val exampleLength = 200              // Length of each training example sequence to use. This could certainly be
    // increased
    val tbpttLength = 50                  // Length for truncated backpropagation through time. i.e., do
    // parameter updates ever 50 characters
    val numEpochs = 20                    // Total number of training epochs
    val generateSamplesEveryNMinibatches = 20   // How frequently to generate samples from the network? 1000
    // characters / 50 tbptt length: 20 parameter updates per minibatch
    val nCharactersToSample = 300         // Length of each sample to generate
    val generationInitialization = null   // Optional character initialization; a random character is used if null
    // Above is Used to 'prime' the LSTM with a character sequence to continue/complete.
    // Initialization characters must all be in CharacterIterator.getMinimalCharacterSet() by default
    val randomSeed = 12345
    val rng = new Random(randomSeed)
    val validCharacters = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZabcdefghijklmnñopqrstuvwxyzáéíóú1234567890\"\n',.?;()[]{}:!-#@ "
//    val validCharacters = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZabcdefghijklmnñopqrstuvwxyzáéíóú1234567890\"\n',.?;()[]{}:!- "

    val data = IOUtils.toString(new FileInputStream("dataSet.txt"), "UTF-8")
    val iter: CharacterIterator = getCharacterExampleIterator(miniBatchSize, exampleLength, validCharacters, rng, data)
    val nOut = iter.totalOutcomes()

    // Set up network configuration:
    val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .seed(randomSeed)
      .l2(0.0001)
      .weightInit(WeightInit.XAVIER)
      .updater(new Adam(0.005))
      .list()
      .layer(new LSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
        .activation(Activation.TANH).build())
      .layer(new DropoutLayer(0.8))
      .layer(new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
        .activation(Activation.TANH).build())
      .layer(new DropoutLayer(0.8))
      .layer(new RnnOutputLayer.Builder(LossFunction.MCXENT).activation(Activation.SOFTMAX)   // MCXENT + softmax for
        // classification
        .nIn(lstmLayerSize).nOut(nOut).build())
      .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
      .build()


    val net = new MultiLayerNetwork(conf)
    net.init()
    net.setListeners(new ScoreIterationListener(1))

    logger.debug(net.summary())

    // Do training, and then generate and print samples from network
    var miniBatchNumber = 0
    for (i <- 0 until numEpochs) {
      logger.debug("Epoch: " + i)
      while (iter.hasNext) {
        val ds: DataSet = iter.next
        net.fit(ds)
        miniBatchNumber = miniBatchNumber + 1
        if (miniBatchNumber % generateSamplesEveryNMinibatches == 0) {
          logger.debug("--------------------")
          logger.debug("Completed " + miniBatchNumber + " minibatches of size " + miniBatchSize + "x" + exampleLength + " characters")
          logger.debug("Sampling characters from network given initialization \"" + (if (generationInitialization == null) ""
          else generationInitialization) + "\"")
          val samples: String = sampleCharactersFromNetwork(generationInitialization, net, iter, rng, nCharactersToSample)
          logger.debug("----- Sample -----")
          logger.debug(samples.toString + "\n")
        }
      }
      iter.reset()
    }

    val locationToSave = new File("datasetv4.zip")
    net.save(locationToSave, true)
  }

  def getCharacterExampleIterator(miniBatchSize: Int, exampleLength: Int, validCharacters: String, rng: Random,
                                  data: String): CharacterIterator = {
    new CharacterIterator(miniBatchSize, exampleLength, validCharacters, rng, data)
  }

  def getCharacter(initialization: String): String = {
    if (initialization == null) {
      "a"
    }
    else {
      initialization
    }
  }

  def sampleCharactersFromNetwork(initialization: String, net: MultiLayerNetwork, iter: CharacterIterator, rng: Random,
                                  charactersToSample: Int): String = {
    // Set up initialization. If no initialization: use a random character
    val ownInitialization: String = getCharacter(initialization)

    val initializationInput: INDArray = Nd4j.zeros(1, iter.inputColumns(), ownInitialization.length)
    val init: Array[Char] = ownInitialization.toCharArray
    for (i <- 0 until ownInitialization.length) {
      val idx: Int = iter.convertCharacterToIndex(init(i))
      initializationInput.putScalar(Array[Int](1, idx, i), 1.0f)
    }

    val sb: StringBuilder = new StringBuilder(ownInitialization)
    net.rnnClearPreviousState()
    var output = net.rnnTimeStep(initializationInput)
    output = output.tensorAlongDimension(output.size(2)-1, 1, 0)

    for (i <- 0 until charactersToSample) {
      val nextInput: INDArray = Nd4j.zeros(1, iter.inputColumns().toLong)
      val cumsum = Nd4j.cumsum(output, 1)

      val sampledCharacterIdx = BooleanIndexing.firstIndex(cumsum.getRow(0), Conditions.greaterThan(rng.nextDouble())
      ).getInt(0)
      nextInput.putScalar(Array[Int](0, sampledCharacterIdx), 1.0f)
      sb.append(iter.convertIndexToChar(sampledCharacterIdx))
      output = net.rnnTimeStep(nextInput)
    }
    sb.toString()
  }



}
