package utilities.dates

import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date}

import scala.collection.JavaConversions._

import org.apache.logging.log4j.scala.Logging

import model.StatusImpl
import model.exceptions.WrongParamValueException

import app.twitterAPI.TwitterServiceOperations.{getAllActionsOrderedByDate, getCSVSeparator}

import utilities.validations.ValidationsUtilTrait


/** Trait that contains dates functionality. */
trait DatesUtilTrait extends Logging with ValidationsUtilTrait {

  val oneMinuteInMillis = 60000

  /**
   * @return Oldest hour of the year 2000
   */
  def getOldestHour: Date = {
    val oldestYear = 2000
    val calendar = getCalendarInstance
    calendar.setTime(new Date())
    calendar.set(Calendar.YEAR, oldestYear)
    calendar.getTime
  }

  /** Group tweets, replies and retweets by date.
   *
   * @param tweets Tweets recovered from Twitter API.
   * @param csvTweets Tweets recovered from Twint.
   * @return Tweets grouped by date.
   */
  def groupTwitterActionsByDates(tweets: Seq[StatusImpl], csvTweets: util.ArrayList[String])
  : Iterable[Map[Int, Seq[String]]] = {

    checkNotEmptySeq(tweets)
    checkNotEmptySeq(csvTweets)

    val orderedDates = getAllActionsOrderedByDate(tweets, csvTweets)

    val apiPattern = "EEE MMM dd HH:mm:ss z yyyy"
    val simpleDateFormatAPI = getSimpleDateFormat(apiPattern)
    val calendar = getCalendarInstance
    val groupByDayOfYearAndHour = orderedDates.groupBy( orderedDate => {
      val date = simpleDateFormatAPI.parse(orderedDate.split(getCSVSeparator)(0))
      calendar.setTime(date)
      calendar.get(Calendar.HOUR_OF_DAY)
    })
    val dayOfYearAndHourMap = groupByDayOfYearAndHour.map(_._2.groupBy(element => {
      val date = simpleDateFormatAPI.parse(element.split(getCSVSeparator)(0))
      calendar.setTime(date)
      calendar.get(Calendar.DAY_OF_YEAR)
    }))
    dayOfYearAndHourMap
  }

  /** Build date.
   *
   * @param dayOfWeek Day of week of the new date.
   * @param hourOfDay Hour of day of the new date.
   * @return New date built with params.
   */
  def buildDate(dayOfWeek: Int, hourOfDay: Int): Date = {
    val calendar = getCalendarInstance
    val date = calendar.getTime
    calendar.setTime(date)
    calendar.set(Calendar.DAY_OF_WEEK, checkDayOfWeek(dayOfWeek))
    setCalendarTime(calendar, hourOfDay)
    calendar.getTime
  }

  private def setCalendarTime(calendar: Calendar, hourOfDay: Int): Unit = {
    calendar.set(Calendar.HOUR_OF_DAY, checkHourOfDay(hourOfDay))
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
  }

  /** Build date.
   *
   * @param hourOfDay Hour of day of the new date.
   * @param dayOfMonth Day of week of the new date.
   * @param month Month of the new date.
   * @param year Year of the new date.
   * @return New date built with params.
   */
  def buildDate(hourOfDay: Int, dayOfMonth: Int, month: Int, year: Int ): Date = {
    val calendar = getCalendarInstance
    val date = calendar.getTime
    calendar.setTime(date)
    try {
      calendar.set(Calendar.YEAR, checkYear(year))
      calendar.set(Calendar.MONTH, checkMonth(month))
      calendar.set(Calendar.DAY_OF_MONTH, checkDay(dayOfMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)))
      calendar.set(Calendar.HOUR_OF_DAY, checkHourOfDay(hourOfDay))
      setCalendarTime(calendar, hourOfDay)
      calendar.getTime
    }
    catch {
      case exception: WrongParamValueException => logger.error(exception.msg)
      new Date()
    }
  }


  /** Wait for one minute. */
  def waitForNextAction(): Unit = {
    val calendar = getCalendarInstance
    val now = calendar.getTime
    calendar.add(Calendar.MINUTE, 1)
    val afterOneMinute = calendar.getTime
    waitForDate(afterOneMinute, now)
  }


  /** Wait for a date
   *
   * @param dateToWaitFor Date to wait for.
   * @param currentDate Current date.
   * @return True if current date is after date to wait for. False if it is not.
   */
  def waitForDate(dateToWaitFor: Date, currentDate: Date): Boolean = {
    if (currentDate.after(dateToWaitFor)) {
      true
    }
    else {
      try { Thread.sleep(oneMinuteInMillis/2) }
      catch { case e: InterruptedException => e.printStackTrace() }
      waitForDate(dateToWaitFor, new Date())
    }
  }

  /** Wait for one hour. */
  def waitForNextHour(): Unit = {
    val calendar = getCalendarInstance
    calendar.add(Calendar.HOUR, 1)
    val nextHour = calendar.getTime
    waitForDate(nextHour, new Date())
  }
  /**
   * @return Calendar instance.
   */
  def getCalendarInstance: Calendar = {
    Calendar.getInstance()
  }

  /** Get calendar day of week from Calendar instance.
   *
   * @param calendar Calendar instance.
   * @return Day of week.
   */
  def getCalendarDay(calendar: Calendar): Int = {
    calendar.get(Calendar.DAY_OF_WEEK)
  }

  /** Get calendar hour of day from calendar instance.
   *
   * @param calendar Calendar instance.
   * @return Hour of day.
   */
  def getCalendarHour(calendar: Calendar): Int = {
    calendar.get(Calendar.HOUR_OF_DAY)
  }

  /** Get simple date format object with the given pattern.
   *
   * @param pattern Patter of the simple date format object.
   * @return Simple date format with the given pattern.
   */
  def getSimpleDateFormat(pattern: String): SimpleDateFormat = {
    checkNotEmptyString(pattern)
    new SimpleDateFormat(pattern)
  }

  /** Get first day of month for a given date.
   *
   * @param date Date object.
   * @return First day of month date.
   */
  def getFirstDayOfMonthDate(date: Date): Date = {
    val calendar = getCalendarInstance
    calendar.setTime(date)
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.getTime
  }

}
