package test.scala.twitterapiTest

import model.exceptions.IncorrectSizeListException
import org.apache.logging.log4j.scala.Logging
import org.junit.Assert
import org.junit.jupiter.api.Test
import twitterapi.TwitterFilter.{cleanTweets, markTweets}

object TwitterFilterTest extends Logging {

  val emptyListExceptionMessage: String = "List can not be empty"

  @Test
  def cleanTweetsTest(): Unit = {

    val spanish: Boolean = true

    // Empty list
    val postSeq: Seq[String] = Seq[String]()
    try {
      cleanTweets(postSeq, spanish)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // Lan esp
    val sentence1esp: String = "Esto es una FRASE que FILTRAR y LIMPIAR. el problema es que las frases" +
      " necesitan tener al menos ochenta caracteres para evitar que sean filtradas ya que las frases de menos de " +
      "ochenta caracteres no son relevantes"
    val sentence2esp: String = "Esto es una frase d ejemplo x q es necesario dar ejemplos. el problema es que las " +
      "frases" +
      " necesitan tener al menos ochenta caracteres para evitar que sean filtradas ya que las frases de menos de " +
      "ochenta caracteres no son relevantes"
    val espPostsSeq: Seq[String] = Seq[String](sentence1esp, sentence2esp)
    val filtered = cleanTweets(espPostsSeq, spanish)
    Assert.assertEquals(sentence1esp.toLowerCase(), filtered(0))
    Assert.assertEquals("esto es una frase de ejemplo por que es necesario dar ejemplos. el problema es que las " +
      "frases necesitan tener al menos ochenta caracteres para evitar que sean filtradas ya que las frases de menos de " +
      "ochenta caracteres no son relevantes", filtered(1))


    // Lan eng
    val sentence1eng: String = "This is a SENTENCE that will be FILTERED and CLEANED. The problem is that those " +
      "sentences needs to have more than 80 characters to not be filtered because short sentences are not relevant " +
      "enough"
    val sentence2eng: String = "This is a sentence to be filtered and cleaned. ty for reading these lines, u will " +
      "have a good day and remember, do your stuff ASAP "
    val engPostsSeq: Seq[String] = Seq[String](sentence1eng, sentence2eng)
    val filteredEng = cleanTweets(engPostsSeq, !spanish)
    Assert.assertEquals(sentence1eng.toLowerCase, filteredEng(0))
    Assert.assertEquals("this is a sentence to be filtered and cleaned. thank you for reading these lines, " +
      "you will have a good day and remember, do your stuff as soon as possible ", filteredEng(1))
  }

  @Test
  def markTweetsTest(): Unit = {
    // empty list
    val emptySeq: Seq[String] = Seq[String]()
    try {
      markTweets(emptySeq)
    }
    catch {
      case exception: IncorrectSizeListException => Assert.assertEquals(emptyListExceptionMessage, exception.msg)
    }
    val sentence1: String = "this is a sentence to mark"
    val sentence2: String = "this is another one"
    val seq: Seq[String] = Seq[String](sentence1, sentence2)
    val marked = markTweets(seq)
    Assert.assertEquals(sentence1 + "\n" + sentence2 + "\n", marked(0) + marked(1))
  }

}
