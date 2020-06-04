package rnn

import org.deeplearning4j.nn.weights.WeightInit

case class NeuralNetworkConfItem(seed: Int,
                                 biasInit: Int,
                                 miniBatch: Boolean,
                                 learningRate: Double,
                                 weightInit: WeightInit,
                                 l2: Double,
                                 tbpttLength: Int)
