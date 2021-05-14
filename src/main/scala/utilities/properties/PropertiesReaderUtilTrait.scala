package utilities.properties

import java.io.InputStream
import java.util.Properties

/** Trait that contains functionality to work with properties file */
trait PropertiesReaderUtilTrait {

  private val properties: Properties = new Properties()
  private val propertiesPath: String = "/config.properties"

  /**
   * @return Properties file content.
   */
  def getProperties: Properties = {
    if (properties.isEmpty) {
      val inputStream: InputStream = this.getClass.getResourceAsStream(propertiesPath)
      properties.load(inputStream)
    }
    properties
  }
}
