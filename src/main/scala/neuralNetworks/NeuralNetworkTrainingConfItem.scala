package neuralNetworks

case class NeuralNetworkTrainingConfItem(miniBatchSize: Int,
                                         exampleLength: Int,
                                         numEpochs: Int,
                                         generateSamplesEveryNMinibatches: Int,
                                         nCharactersToSample: Int,
                                         generationInitialization: String)
