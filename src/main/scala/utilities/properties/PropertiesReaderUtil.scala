package utilities.properties

import java.io.{FileInputStream, FileOutputStream, IOException, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.util.Properties

import org.apache.logging.log4j.scala.Logging

trait PropertiesReaderUtil extends Logging {

  private val properties: Properties = new Properties()

  def getProperties: Properties = {
    if (properties.isEmpty) {
      properties.load(new FileInputStream("src/main/resources/config.properties"))
    }
    properties
  }

  def saveProperties(): Unit = {
    try {
      val out: OutputStreamWriter = new OutputStreamWriter(new FileOutputStream("src/main/resources/config.properties"),
        StandardCharsets.UTF_8)
      properties.store(out, "Updated properties file")
    }
    catch {
      case e: IOException => logger.debug(e)
    }
  }



}
