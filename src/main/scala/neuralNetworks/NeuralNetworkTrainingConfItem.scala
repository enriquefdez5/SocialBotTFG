package neuralNetworks

/** Case class containing training parameters.
 *
 * @constructor Create a new neural network training configuration item with the given parameters.
 * @param miniBatchSize Minibatch size.
 * @param exampleLength Length of example.
 * @param numEpochs Number of epochs.
 * @param generateSamplesEveryNMinibatches Number of minibatches to generate samples.
 * @param nCharactersToSample Number of characters to sample.
 * @param generationInitialization Initialization string.
 */
case class NeuralNetworkTrainingConfItem(miniBatchSize: Int,
                                         exampleLength: Int,
                                         numEpochs: Int,
                                         generateSamplesEveryNMinibatches: Int,
                                         nCharactersToSample: Int,
                                         generationInitialization: String)
