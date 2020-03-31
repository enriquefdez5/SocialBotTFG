package utilities

import java.io.{BufferedWriter, File, FileWriter, IOException}

import org.apache.logging.log4j.scala.Logging


object IOUtil extends Logging {

  /**
   * This function write a Seq of Strings into a file.
   * @param posts The text that should be written on the file
   * @param fileName. The file where the text should be written. By default it writes on the RNN dataSet.txt file
   *                that later will be use to train the RNN
   */
  def writeDataOnAFile(posts: Seq[String], fileName: String = "./dataSet.txt"): Unit = {
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
