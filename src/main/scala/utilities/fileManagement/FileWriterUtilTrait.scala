package utilities.fileManagement

import java.io.{BufferedWriter, File, FileWriter, IOException}
import org.apache.logging.log4j.scala.Logging

/** Trait that contains file writing functionality. */
trait FileWriterUtilTrait extends Logging {


  /** Write text on a file.
   *
   * @param data Data to write into the file.
   * @param fileName Path to the file to write into.
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
    bw.close()
  }

  private def getBufferedWriter(fileName: String): BufferedWriter = {
    new BufferedWriter(new FileWriter(new File(fileName), true))
  }

  /** Create directories needed to write system files into. It does nothing if folders already exists. */
  def createDirectories(): Unit = {
    val manualDataDirectory = new File("./data(manual)/")
    manualDataDirectory.mkdir()
    manualDataDirectory.setWritable(true)
    manualDataDirectory.setReadable(true)
    val generatedDataDirectory = new File("./data(generated)/")
    generatedDataDirectory.mkdir()
    generatedDataDirectory.setWritable(true)
    generatedDataDirectory.setReadable(true)
    val modelsDirectory = new File("./models/")
    modelsDirectory.mkdir()
    modelsDirectory.setWritable(true)
    modelsDirectory.setReadable(true)
  }
}
