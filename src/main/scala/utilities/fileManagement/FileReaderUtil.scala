package utilities.fileManagement

import java.io.{BufferedReader, File, FileInputStream, FileReader, IOException}
import java.util
import java.util.Properties

import org.apache.logging.log4j.scala.Logging
import utilities.properties.PropertiesReaderUtil.{getProperties, properties}

import scala.annotation.tailrec

object FileReaderUtil extends Logging {

  def readCSVFile(fileName: String = getProperties.getProperty("csvTweetsFileName")): util.ArrayList[String] = {
    val file = new File(fileName)
    val br = new BufferedReader(new FileReader(file))
    val result = new util.ArrayList[String]()
    try{
      addCSVData(result, br, br.readLine)
    }
    catch {
      case ioexc: IOException =>
        logger.error("Ups! Something went wrong reading from the file", ioexc)
    }
    br.close()
    result
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
