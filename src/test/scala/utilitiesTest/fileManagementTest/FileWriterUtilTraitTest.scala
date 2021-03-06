package utilitiesTest.fileManagementTest

import java.io.{File, FileNotFoundException}

import org.junit.jupiter.api.Assertions.{assertNotNull, assertTrue}
import org.junit.jupiter.api.Test
import utilities.fileManagement.FileWriterUtilTrait

class FileWriterUtilTraitTest extends FileWriterUtilTrait {

  @Test
  def writeDataOnAFileTest(): Unit = {
    // Writing data on existing file
    // Set up parameters
    val dataContent = "This is the data that will be written on the file"
    val data: Seq[String] = Seq[String](dataContent)
    val fileRoute = "./src/test/resources/writeDataOnAFileTestFile.txt"

    // Do action
    writeDataOnAFile(data, fileRoute)

    // Check it is okey
    val file = new File(fileRoute)
    assertTrue(file.exists())

    // Writing data on non existing file
//    val wrongFileRoute = ".NonExistingFileRoute.txt"
    val wrongFileRoute = "./src/test/resources/NonExistingFileRoute.txt"
    try {
      writeDataOnAFile(data, wrongFileRoute)
    }
    catch {
      case exception: FileNotFoundException => assertNotNull(exception)
    }
  }

  @Test
  def createDirectoriesTest(): Unit = {
    createDirectories()
    assertTrue(new File("./data(generated)/").exists())
    assertTrue(new File("./data(manual)/").exists())
    assertTrue(new File("./models/").exists())
  }
}
