package utilities.fileManagement

import java.io.{BufferedWriter, File, FileWriter, IOException}
import java.util

import org.apache.logging.log4j.scala.Logging
import utilities.properties.PropertiesReaderUtil.{getProperties, properties}

object FileWriterUtil extends Logging{


  /**
   * This function write a Seq of Strings into a file.
   * @param posts The text that should be written on the file
   * @param fileName. The file where the text should be written. By default it writes on the RNN datasetTexto.txt file
   *                that later will be use to train the RNN
   */
  def writeDataOnAFile(posts: Seq[String], fileName: String = getProperties.getProperty("dataSetFileName")): Unit = {
    val bw = getBufferedWriter(fileName)
    try {
      posts.foreach{ post =>
        bw.write(post)
      }
    }
    catch {
      case ioexc: IOException =>
        logger.error("Ups! Something went wrong writing on the file", ioexc)
    }
    // Closing the buffer
    bw.close()
  }

  /**
   * // TODO(check if it is used)
   * Private function for logging training loss scores into a file.
   *
   * @param scores, ArrayList[String]. List of string containing score values to be saved on a file.
   * @param fileName, String. File name where scores will be saved.
   */
  private def writeScores(scores: util.ArrayList[String], fileName: String): Unit = {
    val bw = getBufferedWriter(fileName)
    try {
      for ( i <- 0 until scores.size()) {
        bw.write(scores.get(i))
      }
    }
    catch {
      case ioexc: IOException =>
        logger.error("Ups! Something went wrong writing on the file", ioexc)
    }
    // Closing the buffer
    bw.close()
  }

  private def getBufferedWriter(fileName: String): BufferedWriter = {
    new BufferedWriter(new FileWriter(new File(fileName)))
  }


}
