package twitterapi

import twitter4j.Status

class TwitterFilter {

  /**
   * This function cleans tweets removing mentions(@Someone) and removing
   * links(http://www.anyWebPage.com/parameters or https...)
   * @param tweets. A Seq of Status(twitter4j class that contais tweet info) to be cleaned
   * @return A Seq of Strings containing the Status text without mentions or links.
   */
  def cleanTweets(tweets: Seq[Status]): Seq[String] ={
    val debug = false
    val textFromTweets = tweets.map{ _.getText }
    val textWithoutMentions = textFromTweets.map{ _.replaceAll("@[A-Za-z0-9-_]*", "") }
    val textWithoutMentionsNorLinks = textWithoutMentions.map{ _.replaceAll("http[A-Za-z0-9-_:./?]*", "") }
    if (debug){
      println("-----------Se imprime el texto en el que se debería haber cambiado los @mierdas por un espacio----------")
      println(textWithoutMentionsNorLinks)
    }
    textWithoutMentionsNorLinks    //return is omitted


    //  Test to get regex. Must be deleted
    //
    //    val texto = "Esta es la segunda parte del procesado de datos para borrar cualquier enlace como este " +
    //      "https://regexr.com/ u otro con parámetros como este otro https://users/getUser?1322839_asisdh_87"
    //    val textoProcesado = texto.replaceAll("http[A-Za-z0-9-_:./?]*", "")
    //    println("Este era el texto: \n" + texto )
    //
    //    println("Este es el texto procesado: \n" + textoProcesado)
  }

}
