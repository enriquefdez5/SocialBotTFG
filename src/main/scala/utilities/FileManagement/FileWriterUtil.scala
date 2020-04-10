package utilities.FileManagement

import java.io.{BufferedWriter, File, FileInputStream, FileWriter, IOException}
import java.util.Properties

import org.apache.logging.log4j.scala.Logging

object FileWriterUtil extends Logging{

  // Read properties file
  val properties: Properties = new Properties()
  properties.load(new FileInputStream("src/main/resources/config.properties"))

  /**
   * This function write a Seq of Strings into a file.
   * @param posts The text that should be written on the file
   * @param fileName. The file where the text should be written. By default it writes on the RNN dataSet.txt file
   *                that later will be use to train the RNN
   */
  def writeDataOnAFile(posts: Seq[String], fileName: String = properties.getProperty("dataSetFileName")): Unit = {
    // FileWriter
    val file = new File(fileName)
    val bw = new BufferedWriter(new FileWriter(file))

    try {
      posts.foreach{ item =>
        bw.write(item)
      }
    }
    catch {
      case ioexc: IOException =>
        logger.error("Ups! Something went wrong writing on the file", ioexc)
    }
    // Closing the buffer
    bw.close()
  }

}
