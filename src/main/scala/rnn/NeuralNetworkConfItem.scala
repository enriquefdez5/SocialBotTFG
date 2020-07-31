package rnn

import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.Updater
import org.deeplearning4j.nn.weights.WeightInit

case class NeuralNetworkConfItem(seed: Int,
                                 biasInit: Double,
                                 miniBatch: Boolean,
                                 learningRate: Double,
                                 weightInit: WeightInit,
                                 l2: Double,
                                 tbpttLength: Int,
                                 optimizationAlgorithm: OptimizationAlgorithm,
                                 rmsDecay: Double
                                )
