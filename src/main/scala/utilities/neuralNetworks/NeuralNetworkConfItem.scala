package utilities.neuralNetworks

import org.deeplearning4j.nn.conf.BackpropType
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

case class NeuralNetworkConfItem(seed: Int,
                                 learningRate: Double,
                                 weightInit: WeightInit,
                                 lossFunction: LossFunction,
                                 activationLSTM: Activation,
                                 activationRNN: Activation,
                                 l2: Double,
                                 tbpttType: BackpropType,
                                 tbpttLength: Int,
                                 dropOut: Double,
                                 layerWidth: Int,
                                 layerCount: Int
                                )
