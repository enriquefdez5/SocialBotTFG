package utilities.fileManagement

import java.io.{BufferedReader, File, FileNotFoundException, FileReader}
import java.util
import org.apache.logging.log4j.scala.Logging
import scala.annotation.tailrec

/** Trait that contains file reading operations */
trait FileReaderUtilTrait extends Logging {

  /** Read csv file.
   *
   * @param fileName Path of the file to read.
   * @return Csv file content.
   */
  def readCSVFile(fileName: String): util.ArrayList[String] = {
    val file = new File(fileName)
    try {
      val br = new BufferedReader(new FileReader(file))
      val result = new util.ArrayList[String]()
      addCSVData(result, br, br.readLine)
      br.close()
      result
    }
    catch {
      case exception: FileNotFoundException =>
        logger.error(exception.getMessage)
        new util.ArrayList[String]()
    }
  }

  @tailrec
  private def addCSVData(result: util.ArrayList[String], br: BufferedReader, line: String): util.ArrayList[String] = {
    if (line != null) {
      result.add(line)
      addCSVData(result, br, br.readLine)
    }
    else { result }
  }

}
