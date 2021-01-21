package utilities.properties

import java.io.FileInputStream
import java.util.Properties

object PropertiesReader {

  val properties: Properties = new Properties()

  def getProperties(): Properties = {
    if (properties.isEmpty) {
      properties.load(new FileInputStream("src/main/resources/config.properties"))
    }
    properties
  }
}
