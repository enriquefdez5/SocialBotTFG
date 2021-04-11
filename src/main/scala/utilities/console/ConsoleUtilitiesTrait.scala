package utilities.console


import app.twitterAPI.{ConfigRun, TwitterClientTrait}
import org.apache.logging.log4j.scala.Logging
import twitter4j.{TwitterException, User}

import scala.io.StdIn.readLine

trait ConsoleUtilitiesTrait extends Logging with TwitterClientTrait {

  def askForTwitterUsername(configRun: ConfigRun): String = {
    val username: String = readLine("Type in twitter username to get tweets from \n")
    val twitter = getTwitterClient(configRun)
    try {
      val user = twitter.showUser(username)
      showUserInfo(user)
      username
    }
    catch {
      case exception: TwitterException =>
        logger.info(exception.getMessage)
        logger.info("User with username: \"@" + username + "\" does not exist. Please type a valid username.")
        askForTwitterUsername(configRun)
    }
  }

  private def showUserInfo(user: User): Unit = {
    logger.info("User found!")
    logger.info("User id: " + user.getId)
    logger.info("User name: " + user.getName)
    logger.info("User description: " + user.getDescription)
  }


  def askForLanguage(): Boolean = {
    val language: String = readLine("Type in user language S for spanish, E for english \n")
    if (language != "E" && language != "S") {
      logger.info("Typed language does not match any valid option. Please type a valid option.")
      askForLanguage()
    }
    else {
      if (language == "S") {
        logger.info("Spanish language selected")
        true
      }
      else {
        logger.info("English language selected")
        false
      }
    }
  }
}
