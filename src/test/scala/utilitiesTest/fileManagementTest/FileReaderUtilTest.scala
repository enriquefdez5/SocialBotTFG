package test.scala.utilitiesTest.fileManagementTest


import java.io.FileNotFoundException

import org.junit.Assert
import org.junit.jupiter.api.Test
import utilities.fileManagement.FileReaderUtil

object FileReaderUtilTest extends FileReaderUtil {

  @Test
  def readCSVFileTest(): Unit = {
    // Existing file
    val csvContent = readCSVFile()
    Assert.assertNotNull(csvContent)
    Assert.assertEquals(
      "date,username,to,replies,retweets,favorites,text,geo,mentions,hashtags,id,permalink",
      csvContent.get(0))

    // Non existing file
    try {
      readCSVFile("nonExistingRoute.csv")
    }
    catch {
      case exception: FileNotFoundException => Assert.assertNotNull(exception)
    }
  }
}
