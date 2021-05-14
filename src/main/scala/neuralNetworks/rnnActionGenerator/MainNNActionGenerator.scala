package neuralNetworks.rnnActionGenerator

import java.util.concurrent.TimeUnit
import java.util.Properties
import scala.annotation.tailrec

import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.earlystopping.{EarlyStoppingConfiguration, EarlyStoppingResult}
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator
import org.deeplearning4j.earlystopping.termination.{MaxEpochsTerminationCondition, MaxTimeIterationTerminationCondition}
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer
import org.deeplearning4j.nn.conf.BackpropType
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.evaluation.regression.RegressionEvaluation
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

import app.twitterAPI.ConfigRun

import neuralNetworks.{NeuralNetworkConfItem, NeuralNetworkTrainingConfItem, NeuralNetworkTrainingTrait}

import utilities.console.ConsoleUtilTrait
import utilities.properties.PropertiesReaderUtilTrait


/** Object with main method for action neural network training. */
object MainNNActionGenerator extends Logging with ConsoleUtilTrait with PropertiesReaderUtilTrait
                             with NeuralNetworkTrainingTrait {

  /** Main method for actions neural network training.
   *
   * @param args Item needed to interact with Twitter API.
   */
  def main(args: Array[String]): Unit = {
    val conf = new ConfigRun(args)

    val twitterUsernameMsg: String = "Type in twitter username to imitate"
    val twitterUsername: String = askForTwitterUsername(conf, twitterUsernameMsg)

    val confItem: NeuralNetworkConfItem = createNeuralNetworkConfItem(getProperties)
    val trainingConfItem: NeuralNetworkTrainingConfItem = createNeuralNetworkTrainingConfItem(getProperties)

    val isTextFile = false
    val splitData = getData(twitterUsername, isTextFile)

    val splitSize: Int = (splitData.length * 80) / 100
    val trainingData = getTrainingData(splitData, splitSize)
    val testingData = getTestData(splitData, splitSize)

    val trainingIter = new ActionGeneratorIterator(trainingConfItem.miniBatchSize,
                                                   trainingConfItem.exampleLength,
                                                   trainingData)
    val testIter = new ActionGeneratorIterator(trainingConfItem.miniBatchSize,
                                               trainingConfItem.exampleLength,
                                               testingData)

    val net: MultiLayerNetwork = createAndConfigureNetwork(trainingIter, confItem)

    val maxEpochNumber = 1000
    val maxTimeAmount = 300
    val esConf: EarlyStoppingConfiguration[MultiLayerNetwork] = new EarlyStoppingConfiguration.Builder()
      .epochTerminationConditions(new MaxEpochsTerminationCondition(maxEpochNumber))
      .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(maxTimeAmount, TimeUnit.MINUTES))
      .scoreCalculator(new DataSetLossCalculator(testIter, true))
      .evaluateEveryNEpochs(1)
      .modelSaver(new LocalFileModelSaver("./models/"))
      .build()

    val trainer: EarlyStoppingTrainer = new EarlyStoppingTrainer(esConf, net, trainingIter)
    val result: EarlyStoppingResult[MultiLayerNetwork] = trainer.fit()

    logger.info("Termination reason: " + result.getTerminationReason)
    logger.info("Termination details: " + result.getTerminationDetails)
    logger.info("Total epochs: " + result.getTotalEpochs)
    logger.info("Best epoch number: " + result.getBestModelEpoch)
    logger.info("Score at best epoch: " + result.getBestModelScore)

    val bestModel: MultiLayerNetwork = result.getBestModel

    val regEval = evaluateNet(bestModel, testIter)
    logger.info("Training results:" + getSplitSymbol + regEval.stats())

    val netType: String = "Action"
    createPathAndSaveNetwork(net, twitterUsername, netType)
  }


  private def createNeuralNetworkConfItem(properties: Properties): NeuralNetworkConfItem = {
    neuralNetworks.NeuralNetworkConfItem(
      properties.getProperty("trainingActionRandomSeed").toInt,
      properties.getProperty("trainingActionLearningRate").toDouble,
      WeightInit.valueOf(properties.getProperty("trainingActionWeightInit")),
      LossFunction.valueOf(properties.getProperty("trainingActionLossFunction")),
      Activation.valueOf(properties.getProperty("trainingActionActivationLSTM")),
      Activation.valueOf(properties.getProperty("trainingActionActivationRNN")),
      properties.getProperty("trainingActionL2").toDouble,
      BackpropType.valueOf(properties.getProperty("trainingActionTbpttType")),
      properties.getProperty("trainingActionTbpttLength").toInt,
      properties.getProperty("trainingActionDropout").toDouble,
      properties.getProperty("trainingActionHiddenLayerWidth").toInt,
      properties.getProperty("trainingActionHiddenLayerCont").toInt
    )
  }

  private def createNeuralNetworkTrainingConfItem(properties: Properties): NeuralNetworkTrainingConfItem = {
    NeuralNetworkTrainingConfItem(
      properties.getProperty("trainingActionMiniBatchSize").toInt,
      properties.getProperty("trainingActionExampleLength").toInt,
      properties.getProperty("trainingActionNEpochs").toInt,
      properties.getProperty("trainingActionGenerateSamplesEveryNMiniBatches").toInt,
      properties.getProperty("trainingActionNCharactersToGenerate").toInt,
      properties.getProperty("trainingActionInitialization")
    )
  }

  private def evaluateNet(net: MultiLayerNetwork, testIter: ActionGeneratorIterator): RegressionEvaluation = {
    val regEval = new RegressionEvaluation(3)
    testIter.reset()
    eval(regEval, testIter, net)
    testIter.reset()
    regEval
  }

  @tailrec
  private def eval(regEval: RegressionEvaluation, testIter: ActionGeneratorIterator, net: MultiLayerNetwork): Unit = {
    if (testIter.hasNext) {
      net.rnnClearPreviousState()
      val testds = testIter.next()
      val prediction: INDArray = net.rnnTimeStep(testds.getFeatures)
      regEval.evalTimeSeries(testds.getLabels, prediction)
      eval(regEval, testIter, net)
    }
  }
}
