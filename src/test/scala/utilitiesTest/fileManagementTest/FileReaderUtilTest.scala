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
      "id	conversation_id	created_at	date	time	timezone	user_id	username	name	place	tweet	language	" +
        "mentions	urls	photos	replies_count	retweets_count	likes_count	hashtags	cashtags	link	retweet	" +
        "quote_url	video	thumbnail	near	geo	source	user_rt_id	user_rt	retweet_id	reply_to	retweet_date	" +
        "translate	trans_src	trans_dest",
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
