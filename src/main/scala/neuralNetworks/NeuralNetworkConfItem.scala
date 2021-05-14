package neuralNetworks

import org.deeplearning4j.nn.conf.BackpropType
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

/** Case class containing neural network configuration parameters
 *
 * @constructor Create a new neural network configuration item with the given parameters.
 * @param seed Random seed.
 * @param learningRate Learning rate.
 * @param weightInit Weight initialization method.
 * @param lossFunction Loss function type.
 * @param activationLSTM Activation function for LSTM network.
 * @param activationRNN Activation function for RNN network.
 * @param l2 L2 regularization value.
 * @param tbpttType Back propagation type.
 * @param tbpttLength Back propagation length.
 * @param dropOut Dropout value.
 * @param layerWidth Hidden layers width.
 * @param layerCount Layer count.
 */
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
