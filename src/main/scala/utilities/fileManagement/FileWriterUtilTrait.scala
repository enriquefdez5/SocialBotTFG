package utilities.fileManagement

import java.io.{BufferedWriter, File, FileWriter, IOException}
import java.util

import org.apache.logging.log4j.scala.Logging
import utilities.properties.PropertiesReaderUtilTrait

trait FileWriterUtilTrait extends Logging with PropertiesReaderUtilTrait {


  /**
   * This function write a Seq of Strings into a file.
   * @param data The text that should be written on the file
   * @param fileName. The file where the text should be written. By default it writes on the RNN datasetTexto.txt file
   *                that later will be use to train the RNN
   */
  def writeDataOnAFile(data: Seq[String], fileName: String): Unit = {
    val bw = getBufferedWriter(fileName)
    try {
      data.foreach{ line =>
        bw.write(line)
      }
    }
    catch {
      case ioexc: IOException =>
        logger.error("Ups! Something went wrong writing on the file", ioexc)
    }
    // Closing the buffer
    bw.close()
  }

  def createDirectories(): Unit = {
    val manualDataDirectory = new File("./data(manual)/")
    manualDataDirectory.mkdir()
    manualDataDirectory.setWritable(true)
    manualDataDirectory.setReadable(true)
    val generatedDataDirectory = new File("./data(generated)/")
    generatedDataDirectory.mkdir()
    generatedDataDirectory.setWritable(true)
    generatedDataDirectory.setReadable(true)
  }
  private def getBufferedWriter(fileName: String): BufferedWriter = {
    new BufferedWriter(new FileWriter(new File(fileName)))
  }


}
