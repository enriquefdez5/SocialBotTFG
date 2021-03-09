package test.scala.utilitiesTest.propertiesReaderUtilTest

import org.junit.Assert
import org.junit.jupiter.api.Test
import utilities.properties.PropertiesReaderUtil


object PropertiesReaderUtilTest extends PropertiesReaderUtil {

  val propertiesTestFileRoute = "./src/test/configTest.properties"


  @Test
  def getPropertiesTest(): Unit = {
    // Load first time properties file
    Assert.assertFalse(getProperties.isEmpty)

    // Load properties file
    Assert.assertFalse(getProperties.isEmpty)
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
    Assert.assertNotNull(getProperties.getProperty(newPropertyName))
    Assert.assertEquals(
      getProperties.getProperty(newPropertyName),
      newPropertyValue)
  }
}
