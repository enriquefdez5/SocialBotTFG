package utilities

import java.io.{BufferedWriter, File, FileWriter, IOException}

import model.Post

class IOUtil {


  /**
   * This function write a Seq of Strings into a file.
   * @param posts The text that should be written on the file
   * @param fileName. The file where the text should be written. By default it writes on the RNN dataSet.txt file
   *                that later will be use to train the RNN
   */
  def writeDataOnAFile(posts: Seq[String], fileName: String = "./dataSet.txt"):Unit ={
    // FileWriter
    val file = new File(fileName)
    val bw = new BufferedWriter(new FileWriter(file))

    try{
      //It is needed for the RNN to add a mark symbol at the start and at the end of the sentence
      posts.foreach{ item =>
        val sb: StringBuilder = new StringBuilder()
        sb.append("&" + item + "%\n")
        bw.write(sb.toString())
      }
    }
    catch{
      case ioexc: IOException =>  { println("Ups! Something went wrong writing on the file") }
    }
    //Closing the buffer
    bw.close()
  }
}
