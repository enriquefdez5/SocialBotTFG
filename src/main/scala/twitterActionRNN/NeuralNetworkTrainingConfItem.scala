package twitterActionRNN

case class NeuralNetworkTrainingConfItem(miniBatchSize: Int,
                                    exampleLength: Int,
                                    numEpochs: Int,
                                    generateSampleEveryNMinibatches: Int,
                                    nCharactersToSample: Int,
                                    generationInitialization: String)
