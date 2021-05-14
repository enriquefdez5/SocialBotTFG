package twitterapiTest

import app.twitterAPI.TwitterFilterTrait
import model.exceptions.IncorrectSizeListException
import org.apache.logging.log4j.scala.Logging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TwitterFilterTest extends Logging with TwitterFilterTrait {

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
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }

    // Lan esp
    val fillingChars: String = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    val sentence1esp: String = "Esto es una FRASE que FILTRAR y LIMPIAR. " + fillingChars
    val sentence2esp: String = "Esto es una frase d ejemplo x q es necesario dar ejemplos. " + fillingChars
    val espPostsSeq: Seq[String] = Seq[String](sentence1esp, sentence2esp)
    val filtered = cleanTweets(espPostsSeq, spanish)
    assertEquals(sentence1esp.toLowerCase(), filtered(0))
    assertEquals("esto es una frase de ejemplo por que es necesario dar ejemplos. " + fillingChars, filtered(1))


    // Lan eng
    val sentence1eng: String = "This is a SENTENCE that will be FILTERED and CLEANED. " + fillingChars
    val sentence2eng: String = "This is a sentence to be filtered and cleaned. ty for reading these lines, u will " +
      "have a good day and remember, do your stuff ASAP "
    val engPostsSeq: Seq[String] = Seq[String](sentence1eng, sentence2eng)
    val filteredEng = cleanTweets(engPostsSeq, !spanish)
    assertEquals(sentence1eng.toLowerCase, filteredEng(0))
    assertEquals("this is a sentence to be filtered and cleaned. thank you for reading these lines, " +
      "you will have a good day and remember, do your stuff as soon as possible ", filteredEng(1))
  }

  @Test
  def addLineBreakTest(): Unit = {
    // empty list
    val emptySeq: Seq[String] = Seq[String]()
    try {
      addLineBreak(emptySeq)
    }
    catch {
      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
    }
    val sentence1: String = "this is a sentence to mark"
    val sentence2: String = "this is another one"
    val seq: Seq[String] = Seq[String](sentence1, sentence2)
    val marked = addLineBreak(seq)
    assertEquals(sentence1 + "\n" + sentence2 + "\n", marked(0) + marked(1))
  }


}
