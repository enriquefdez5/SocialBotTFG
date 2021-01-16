package utilities

import java.io.{BufferedReader, BufferedWriter, File, FileInputStream, FileReader, FileWriter, IOException}
import java.util
import java.util.Properties

import org.apache.logging.log4j.scala.Logging

import scala.annotation.tailrec


object IOUtil extends Logging {

  // Read properties file
  val properties: Properties = new Properties()
  properties.load(new FileInputStream("src/main/resources/config.properties"))

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
  }}
