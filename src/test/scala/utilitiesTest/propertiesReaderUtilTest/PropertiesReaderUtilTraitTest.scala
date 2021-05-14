package utilitiesTest.propertiesReaderUtilTest

import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertNotNull}
import org.junit.jupiter.api.Test
import utilities.properties.PropertiesReaderUtilTrait


class PropertiesReaderUtilTraitTest extends PropertiesReaderUtilTrait {


  @Test
  def getPropertiesTest(): Unit = {
    // Load first time properties file
    assertFalse(getProperties.isEmpty)
  }

}
