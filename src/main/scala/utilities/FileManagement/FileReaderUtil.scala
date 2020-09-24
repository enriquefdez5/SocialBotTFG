package utilities.FileManagement

import java.io.{BufferedReader, File, FileInputStream, FileReader, IOException}
import java.util
import java.util.Properties

import org.apache.logging.log4j.scala.Logging

import scala.annotation.tailrec

object FileReaderUtil extends Logging {

  // Read properties file
  val properties: Properties = new Properties()
  properties.load(new FileInputStream("src/main/resources/config.properties"))


  def readDataFromAFile(fileName: String = properties.getProperty("dataSetFileName")): StringBuffer = {
    val file = new File(fileName)
    val br = new BufferedReader(new FileReader(file))
    val result = new StringBuffer
    try{
      addData(result, br, br.readLine)
    }
    catch {
      case ioexc: IOException =>
        logger.error("Ups! Something went wrong reading from the file", ioexc)
    }
    br.close()
    result
  }

  @tailrec
  private def addData(result: StringBuffer, br: BufferedReader, line: String): StringBuffer = {
    if (line != null) {
      result.append(line)
      addData(result, br, br.readLine)
    }
    else { result }
  }


  def readCSVFile(fileName: String = properties.getProperty("csvTweetsFileName")): util.ArrayList[String] = {
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
