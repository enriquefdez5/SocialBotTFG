package utilitiesTest.fileManagementTest


import java.io.FileNotFoundException

import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull}
import org.junit.jupiter.api.Test
import utilities.fileManagement.FileReaderUtil

class FileReaderUtilTest extends FileReaderUtil {

  @Test
  def readCSVFileTest(): Unit = {
    // Existing file
    val csvContent = readCSVFile()
    assertNotNull(csvContent)
    assertEquals(
      "date,username,to,replies,retweets,favorites,text,geo,mentions,hashtags,id,permalink",
      csvContent.get(0))

    // Non existing file
    try {
      readCSVFile("nonExistingRoute.csv")
    }
    catch {
      case exception: FileNotFoundException => assertNotNull(exception)
    }
  }
}
