package neuralNetworks

import org.apache.logging.log4j.scala.Logging
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator

trait NeuralNetworkTrainingTrait extends Logging with NeuralNetworkConfTrait {


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
