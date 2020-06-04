package rnn

import java.util.Random

import org.apache.logging.log4j.scala.Logging
import utilities.FileManagement.FileReaderUtil


object RnnMain extends Logging{

  def main(args: Array[String]): Unit = {

    // Read data from file
    val data = FileReaderUtil.readDataFromAFile()
    val splittedData: Array[String] = data.toString.split("%")

    // Train model
    val net = RnnModel.buildNetwork(splittedData)

    // Save trained model
    RnnModel.saveNet(net)



  }
}
