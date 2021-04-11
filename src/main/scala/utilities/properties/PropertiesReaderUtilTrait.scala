package utilities.properties

import java.io.{FileInputStream, FileOutputStream, IOException, InputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.util.Properties

import jdk.xml.internal.SecuritySupport.getResourceAsStream
import org.apache.logging.log4j.scala.Logging

trait PropertiesReaderUtilTrait extends Logging {

  private val properties: Properties = new Properties()
  private val propertiesPath: String = "/config.properties"


  def getProperties: Properties = {
    if (properties.isEmpty) {
//      properties.load(new FileInputStream(propertiesPath))
      val inputStream: InputStream = this.getClass.getResourceAsStream(propertiesPath)
      properties.load(inputStream)
    }
    properties
  }

  def saveProperties(): Unit = {
    try {
      val out: OutputStreamWriter = new OutputStreamWriter(new FileOutputStream(propertiesPath),
        StandardCharsets.UTF_8)
      properties.store(out, "Updated properties file")
    }
    catch {
      case e: IOException => logger.debug(e)
    }
  }



}
