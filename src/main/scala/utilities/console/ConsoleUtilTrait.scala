package utilities.console

import org.apache.logging.log4j.scala.Logging

import java.util
import java.util.{Calendar, Date}

import scala.io.StdIn
import scala.io.StdIn.readLine

import app.actionExecution.MainActionExecution
import app.dataRecovery.MainDataRecovery
import app.twitterAPI.{ConfigRun, TwitterClientTrait}

import neuralNetworks.rnnActionGenerator.MainNNActionGenerator
import neuralNetworks.rnnCharacterGenerator.MainNNCharacterGenerator

import twitter4j.{TwitterException, User}

import utilities.dates.DatesUtilTrait



/** Trait that contains console input and output functions. */
trait ConsoleUtilTrait extends Logging with TwitterClientTrait with DatesUtilTrait {

  val option1: Int = 1
  val option2: Int = 2
  val option3: Int = 3
  val option4: Int = 4
  val exitOption: Int = 0

  /** Show main menu UI and read input */
  def showMainMenuOptions(args: Array[String]): Int = {
    Console.print("\nWhat do you want to do? \n" +
      "1. Recover user data\n" +
      "2. Train neural network for text generation\n" +
      "3. Train neural network for action generation\n" +
      "4. Execute actions\n" +
      "0. Exit\n")
    readMainMenuOption(args)
  }

  private def readMainMenuOption(args: Array[String]): Int = {
    try {
      StdIn.readInt() match {
        case `option1` => MainDataRecovery.main(args)
          option1

        case `option2` => MainNNCharacterGenerator.main(args)
          option2

        case `option3` => MainNNActionGenerator.main(args)
          option3

        case `option4` => MainActionExecution.main(args)
          option4

        case `exitOption` => System.exit(1)
          exitOption

        case _ =>
          logger.warn("Not a valid option, try again.")
          readMainMenuOption(args)
      }
    }
    catch {
      case exception: NumberFormatException =>
        logger.error("Input is not even a number.")
        readMainMenuOption(args)
    }
  }

  /** Show main data recovery execution menu and read input.
   *
   * @param args Main args to build item to interact with Twitter API.
   * @return Selected option.
   */
  def mainDataRecoveryExecutionMainMenu(args: Array[String]): Int = {
    Console.print("\nWhat do you want to do? \n" +
      "1. Scrape all the Tweets of a user\n" +
      "2. Collect Tweets that were tweeted since a given date\n" +
      "3. Back\n" +
      "0. Exit\n")
    readMainDataRecoveryMenuOption(args)
  }

  private def readMainDataRecoveryMenuOption(args: Array[String]): Int = {
    try {
      StdIn.readInt() match {
        case `option1` => option1
        case `option2` => option2
        case `option3` => showMainMenuOptions(args)
          -1
        case `exitOption` => System.exit(1)
          0
        case _ =>
          logger.warn("Not a valid option, try again.\n")
          readMainDataRecoveryMenuOption(args)
      }
    }
    catch {
      case exception: NumberFormatException =>
        logger.error("Input is not even a number.")
        readMainMenuOption(args)
    }

  }

  /** Ask for date.
   *
   * @param selectedOption. Selected option in previous menu.
   * @return Date built with user inputs.
   */
  def askForDate(selectedOption: Int): Date = {
    val newDate = new Date()
    if (selectedOption == 2) {
      Console.print("Indicate the date since tweets will be recovered, please\n")
      val year = askForYear()
      val month = askForMonth() - 1
      val day = askForDay(month)
      val hour = askForHour()
      val date = buildDate(hour, day, month, year)
      if (date.after(newDate)) {
        logger.warn(date.toString + "is an invalid date. Date is after current date and it must be before current date\n")
        askForDate(selectedOption)
      }
      else {
        logger.info("Recovering tweets since: " + date.toString)
        date
      }
    }
    else {
      newDate
    }
  }


  private def askForHour(): Int = {
    Console.print("Type in a number between 0 and 23, both included, that matches with the last tweet date's " +
      "hour\n")
    try {
      val hour = StdIn.readInt()
      if (hour < 0 || hour > 23) {
        logger.warn( hour.toString + " is an invalid hour, please try again\n")
        askForHour()
      }
      else {
        hour
      }
    }
    catch {
      case _: NumberFormatException =>
        logger.error("Invalid hour, please try again\n")
        askForHour()
    }
  }

  private def askForDay(month: Int): Int = {
    val calendar = getCalendarInstance
    calendar.set(Calendar.MONTH, month)
    val maxDay: Int = calendar.getMaximum(Calendar.DAY_OF_MONTH)
    Console.print("Type in a number between 1 and " + maxDay + ", both included, that matches with the last tweet " +
      "date's day\n")
    try {
      val day = StdIn.readInt()
      if (day < 0 || day > maxDay) {
        logger.warn(day.toString + " is an invalid day, please try again\n")
        askForDay(month)
      }
      else {
        day
      }
    }
    catch {
      case _: NumberFormatException =>
        logger.error("Invalid day, please try again\n")
        askForDay(month)
    }
  }

  private def askForMonth(): Int = {
    val minMonthValue = 1
    val maxMonthValue = 12
    Console.print("Type in a number between " + minMonthValue + " and " + maxMonthValue + ", both included, that " +
      "matches with the last tweet date's month\n")
    try {
      val month = StdIn.readInt()
      if (month < minMonthValue || month > maxMonthValue) {
        logger.warn(month.toString + " is an invalid month, please try again\n")
        askForMonth()
      }
      else {
        month
      }
    }
    catch {
      case _: NumberFormatException =>
        logger.error("Invalid month, please try again")
        askForMonth()
    }
  }

  private def askForYear(): Int = {
    val minYearValue = 0
    val calendar = getCalendarInstance
    calendar.setTime(new Date())
    val currentYear = calendar.get(Calendar.YEAR)
      Console.print("Type in a number that matches with the last tweet date's year\n")
    try {
      val year = StdIn.readInt()
      if (year < minYearValue || year > currentYear) {
        logger.warn(year.toString + " is an invalid year, please try again\n")
        askForYear()
      }
      else {
        year
      }
    }
    catch {
      case _: NumberFormatException =>
        logger.error("Invalid year, please try again\n")
        askForYear()
    }
  }


  /** Ask for twitter username.
   *
   * @param configRun Item to access Twitter API.
   * @param msg Custom message to show for asking username.
   * @return Twitter username.
   */
  def askForTwitterUsername(configRun: ConfigRun, msg: String): String = {
    val username: String = readLine(msg + "\n")
    val twitter = getTwitterClient(configRun)
    try {
      val user = twitter.showUser(username)
      showUserInfo(user)
      username
    }
    catch {
      case exception: TwitterException =>
        logger.error(exception.getMessage)
        logger.warn("User with username: \"@" + username + "\" does not exist. Please type a valid username.")
        askForTwitterUsername(configRun, msg)
    }
  }

  private def showUserInfo(user: User): Unit = {
    logger.info("User found!")
    logger.info("User id: " + user.getId)
    logger.info("User name: " + user.getName)
    logger.info("User description: " + user.getDescription)
  }


  /**
   * @return True if selected language is spanish and false if it is english.
   */
  def askForLanguage(): Boolean = {
    val language: String = readLine("Type in user language S for spanish, E for english \n")
    if (language != "E" && language != "S") {
      logger.warn("Typed language does not match any valid option. Please type a valid option.")
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

  /** Show input file information.
   *
   * @param maxSize Maximum file size.
   * @param fileCharacterToReturn Number of characters to return.
   */
  def showFileInfo(maxSize: Int, fileCharacterToReturn: util.ArrayList[Char]): Unit = {
    val fileCharacterSize = fileCharacterToReturn.size
    val nRemoved = maxSize - fileCharacterSize
    logger.info("Loaded and converted file: " + fileCharacterSize + " valid characters of " + maxSize + "" +
      " total characters (" + nRemoved + " removed")
    logger.info("Number of characters in file: " + fileCharacterSize)
  }

}
