package utilitiesTest.propertiesReaderUtilTest

import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertNotNull}
import org.junit.jupiter.api.Test
import utilities.properties.PropertiesReaderUtil


class PropertiesReaderUtilTest extends PropertiesReaderUtil {

  val propertiesTestFileRoute = "./src/test/configTest.properties"


  @Test
  def getPropertiesTest(): Unit = {
    // Load first time properties file
    assertFalse(getProperties.isEmpty)

    // Load properties file
    assertFalse(getProperties.isEmpty)
  }

  @Test
  def savePropertiesTest(): Unit = {
    // Update properties file with new item
    // setting parameters
    val newPropertyName: String = "testPropertyName"
    val newPropertyValue: String = "testPropertyValue"
    getProperties.setProperty(newPropertyName, newPropertyValue)

    // execute action
    saveProperties()

    // test action
    assertNotNull(getProperties.getProperty(newPropertyName))
    assertEquals(
      getProperties.getProperty(newPropertyName),
      newPropertyValue)
  }
}
