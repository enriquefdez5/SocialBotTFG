package utilities.dates

import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date}

import model.Post
import twitterapi.TwitterService.{csvSeparator, getAllActionsOrderedByDate}
import utilities.validations.ValidationsUtil.{checkNotEmptyString, checkNotNull, checkValue, maxDayValue, maxHourValue}

import scala.annotation.tailrec

object datesUtil {

  def groupTwitterActionsByDates(tweets: Seq[Post], csvTweets: util.ArrayList[String])
    : Iterable[Map[Int, Seq[String]]] = {
    checkNotNull(tweets)
    checkNotNull(csvTweets)

    val orderedDates = getAllActionsOrderedByDate(tweets, csvTweets)

    val apiPattern = "EEE MMM dd HH:mm:ss z yyyy"
    val simpleDateFormatAPI = getSimpleDateFormat(apiPattern)
    val calendar = getCalendarInstance
    // grouped by hour and then grouped by day of year
    val groupByDayOfYearAndHour = orderedDates.groupBy( orderedDate => {
      val date = simpleDateFormatAPI.parse(orderedDate.split(csvSeparator)(0))
      calendar.setTime(date)
      calendar.get(Calendar.HOUR_OF_DAY)
    })
    val dayOfYearAndHourMap = groupByDayOfYearAndHour.map(_._2.groupBy(element => {
      val date = simpleDateFormatAPI.parse(element.split(csvSeparator)(0))
      calendar.setTime(date)
      calendar.get(Calendar.DAY_OF_YEAR)
    }))
    dayOfYearAndHourMap
  }

  /**
   * Function that builds date from day and hour values.
   * @param dayOfWeek. Integer that represents the day of the week.
   * @param hourOfDay. Integer that represents the hour of the day.
   * @return a date object with day and hour from param values. Minutes and seconds set to 0.
   */
  def buildDate(dayOfWeek: Int, hourOfDay: Int): Date = {
    checkNotNull(dayOfWeek)
    checkNotNull(hourOfDay)
    checkValue(dayOfWeek, max = maxDayValue)
    checkValue(hourOfDay, max = maxHourValue)

    val calendar = getCalendarInstance
    val date = calendar.getTime
    calendar.setTime(date)
    val computedDayOfWeek = dayOfWeek * 7
    val computedHourOfDay = hourOfDay * 24
    calendar.set(Calendar.DAY_OF_WEEK, computedDayOfWeek)
    calendar.set(Calendar.HOUR_OF_DAY, computedHourOfDay)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.getTime
  }

  /**
   * Function that waits for now date to be after neural network generated date
   * @param dateToWaitFor. Date object that represents a neural network generated date.
   * @param nowDate. Date object that represents actual date.
   * @return Boolean object if now date is after generated date. If not, it waits until that happens.
   */
  @tailrec
  def waitForDate(dateToWaitFor: Date, nowDate: Date): Boolean = {
    checkNotNull(dateToWaitFor)
    checkNotNull(nowDate)

    if (nowDate.after(dateToWaitFor)) {
      true
    }
    else {
      waitForDate(dateToWaitFor, new Date())
    }
  }

  /**
   * Auxiliar function to obtain a calendar instance
   * @return
   */
  def getCalendarInstance: Calendar = {
    Calendar.getInstance()
  }

  /**
   * Function that gets the day of week from a calendar instance
   * @param calendar, Calendar. Calendar object from which day of week will be extracted.
   * @return Int. Value of the day of week contained in the calendar object.
   */
  def getCalendarDay(calendar: Calendar): Int = {
    checkNotNull(calendar)

    calendar.get(Calendar.DAY_OF_WEEK)
  }

  /**
   * Function that gets the hour of the day from a calendar instance
   * @param calendar, Calendar. Calendar object from which day of week will be extracted.
   * @return Int. Value of the hour of the day contained in the calendar object.
   */
  def getCalendarHour(calendar: Calendar): Int = {
    checkNotNull(calendar)

    calendar.get(Calendar.HOUR_OF_DAY)
  }

  /**
   * Function that returns a simple date format with a given string pattern
   * @param pattern, String. Patter to follow to transform any string into a Date object.
   * @return SimpleDateFormat. The simple date format built with the given pattern.
   */
  def getSimpleDateFormat(pattern: String): SimpleDateFormat = {
    checkNotNull(pattern)
    checkNotEmptyString(pattern)

    new SimpleDateFormat(pattern)
  }

  /**
   * Function that returns a Date object containing the first day of the month of a given date.
   * @param date. Date from which the first day of month will be obtained.
   * @return Date. Date object containing the first day of the month of the parameter date.
   */
  def getFirstDayOfMonth(date: Date): Date = {
    checkNotNull(date)

    val calendar = getCalendarInstance
    calendar.setTime(date)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.getTime
  }

}
