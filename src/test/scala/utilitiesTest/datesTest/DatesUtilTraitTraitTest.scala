package utilitiesTest.datesTest

import java.util
import java.util.Calendar

import model.StatusImpl
import model.TypeAndDate.{maxDayValue, maxHourValue, minDayValue, minHourValue}
import model.exceptions.{EmptyStringException, WrongParamValueException}
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertTrue}
import org.junit.jupiter.api.Test
import utilities.dates.DatesUtilTraitTrait

class DatesUtilTraitTraitTest extends DatesUtilTraitTrait {

  val wrongParamValueExceptionMessage = "Param value is not valid"
  val emptyStringExceptionMessage = "The string can not be empty or blank"

  val postSeq: Seq[StatusImpl] = createTestingPostSeq
  val postArrayList: util.ArrayList[String] = createTestingStringArrayList

  @Test
  def buildDateTest(): Unit = {
    // Incorrect day of week value
      // Negative day of week value
    try { buildDate(minDayValue-1, minHourValue) }
    catch {
      case e: WrongParamValueException =>
        assertEquals(wrongParamValueExceptionMessage, e.msg)
    }
      // Greater than 6 day of week value
    try { buildDate(maxDayValue+1, minHourValue) }
    catch {
      case e: WrongParamValueException =>
        assertEquals(wrongParamValueExceptionMessage, e.msg)
    }
    // Incorrect hour of day value
      // Negative hour of day value
    try { buildDate(minDayValue, minHourValue-1) }
    catch {
      case e: WrongParamValueException =>
        assertEquals(wrongParamValueExceptionMessage, e.msg)
    }
      // Greater than 23 hour of day value
    try { buildDate(minDayValue, maxDayValue+1) }
    catch {
      case e: WrongParamValueException =>
        assertEquals(wrongParamValueExceptionMessage, e.msg)
    }
    // Valid day of week and hour of day
    val builtDate = buildDate(0, 0)
    assertNotNull(builtDate)
  }

  @Test
  def getCalendarInstanceTest(): Unit = {
    // Check calendar instance is not null when call
    assertNotNull(getCalendarInstance)
  }

  @Test
  def getCalendarDayTest(): Unit = {
    val calendar = getCalendarInstance

    // First day of week value
    calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMinimum(Calendar.DAY_OF_WEEK))
    assertEquals(getCalendarDay(calendar), calendar.getActualMinimum(Calendar.DAY_OF_WEEK))


    // Last day of week value
    calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK))
    assertEquals(getCalendarDay(calendar), calendar.getActualMaximum(Calendar.DAY_OF_WEEK))


    // One in the middle day of week value
    calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK) / 2)
    assertTrue(
      getCalendarDay(calendar) >= calendar.getActualMinimum(Calendar.DAY_OF_WEEK) &&
        getCalendarDay(calendar) <= calendar.getActualMaximum(Calendar.DAY_OF_WEEK))
  }

  @Test
  def getCalendarHourTest(): Unit = {
    val calendar = getCalendarInstance
    val hourOfDay = getCalendarHour(calendar)
    assertTrue(hourOfDay >= minHourValue && hourOfDay <= maxHourValue)
  }

  @Test
  def getSimpleDateFormatTest(): Unit = {
    // check pattern is the same
    val pattern = "yyyy-MM-dd HH:mm:ss"
    val sdf = getSimpleDateFormat(pattern)
    assertEquals(sdf.toPattern, pattern)

    // check empty pattern raise exception
    val pattern2 = ""
    try { getSimpleDateFormat(pattern2) }
    catch {
      case e: EmptyStringException =>
        assertEquals(emptyStringExceptionMessage, e.msg)
    }
  }

  @Test
  def getFirstDayOfMonthDateTest(): Unit = {
    val oldFirstDayCalendar = getCalendarInstance
    val oldYr = 2020
    val oldMth = 1
    val oldHr = 0
    val oldMin = 0
    val oldScnd = 0
    oldFirstDayCalendar.set(oldYr, oldMth, 1, oldHr, oldMin, oldScnd)

    val halfMonthDay = 15

    val oldFirstDayDate = oldFirstDayCalendar.getTime
    assertEquals(
      oldFirstDayCalendar.getTime,
      getFirstDayOfMonthDate(oldFirstDayDate)
    )

    val halfMonthDayDateCalendar = getCalendarInstance
    halfMonthDayDateCalendar.setTime(oldFirstDayCalendar.getTime)
    halfMonthDayDateCalendar.set(Calendar.DAY_OF_MONTH, halfMonthDay)
    assertEquals(
      oldFirstDayCalendar.getTime,
      getFirstDayOfMonthDate(halfMonthDayDateCalendar.getTime)
    )

    val lastMonthDayDateCalendar = getCalendarInstance
    lastMonthDayDateCalendar.setTime(oldFirstDayCalendar.getTime)
    lastMonthDayDateCalendar.set(Calendar.DAY_OF_MONTH, lastMonthDayDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    assertEquals(
      oldFirstDayCalendar.getTime,
      getFirstDayOfMonthDate(lastMonthDayDateCalendar.getTime)
    )


    // actual
    val actualFirstDayCalendar = getCalendarInstance
    actualFirstDayCalendar.set(Calendar.DAY_OF_MONTH, actualFirstDayCalendar.getActualMinimum(Calendar.DAY_OF_MONTH))
    actualFirstDayCalendar.set(Calendar.HOUR_OF_DAY, actualFirstDayCalendar.getActualMinimum(Calendar.HOUR_OF_DAY))
    actualFirstDayCalendar.set(Calendar.MINUTE, actualFirstDayCalendar.getActualMinimum(Calendar.MINUTE))
    actualFirstDayCalendar.set(Calendar.SECOND, actualFirstDayCalendar.getActualMinimum(Calendar.SECOND))

    assertEquals(
      actualFirstDayCalendar.getTime,
      getFirstDayOfMonthDate(actualFirstDayCalendar.getTime)
    )

    val actualHalfMonthDayDateCalendar = getCalendarInstance
    actualHalfMonthDayDateCalendar.setTime(actualFirstDayCalendar.getTime)
    actualHalfMonthDayDateCalendar.set(Calendar.DAY_OF_MONTH, halfMonthDay)
    assertEquals(
      actualFirstDayCalendar.getTime,
      getFirstDayOfMonthDate(actualHalfMonthDayDateCalendar.getTime)
    )


    val actualLastMonthDayDateCalendar = getCalendarInstance
    actualLastMonthDayDateCalendar.setTime(actualFirstDayCalendar.getTime)
    actualLastMonthDayDateCalendar.set(Calendar.DAY_OF_MONTH, actualLastMonthDayDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    assertEquals(
      actualFirstDayCalendar.getTime,
      getFirstDayOfMonthDate(actualLastMonthDayDateCalendar.getTime)
    )



    // next

    val nextFirstDayCalendar = getCalendarInstance
    val nextYr = 2025
    val nextMth = 12
    val nextHr = 0
    val nextMin = 0
    val nextScnd = 0
    nextFirstDayCalendar.set(nextYr, nextMth, 1, nextHr, nextMin, nextScnd )


    assertEquals(
      nextFirstDayCalendar.getTime,
      getFirstDayOfMonthDate(nextFirstDayCalendar.getTime)
    )

    val nextHalfMonthDayDateCalendar = getCalendarInstance
    nextHalfMonthDayDateCalendar.setTime(nextFirstDayCalendar.getTime)
    nextHalfMonthDayDateCalendar.set(Calendar.DAY_OF_MONTH, halfMonthDay)
    assertEquals(
      nextFirstDayCalendar.getTime,
      getFirstDayOfMonthDate(nextHalfMonthDayDateCalendar.getTime)
    )


    val nextLastMonthDayDateCalendar = getCalendarInstance
    nextLastMonthDayDateCalendar.setTime(nextFirstDayCalendar.getTime)
    nextLastMonthDayDateCalendar.set(Calendar.DAY_OF_MONTH, nextLastMonthDayDateCalendar.getActualMaximum(Calendar
      .DAY_OF_MONTH))
    assertEquals(
      nextFirstDayCalendar.getTime,
      getFirstDayOfMonthDate(nextLastMonthDayDateCalendar.getTime)
    )
  }


  private def createTestingPostSeq: Seq[StatusImpl] = {
    val year = 2020
    val month = 1
    val day = 1
    val hour = 10
    val minute = 0
    val second = 0
    val calendar = getCalendarInstance
    calendar.set(year, month, day, hour, minute, second)
    val date1 = calendar.getTime
    calendar.set(year, month, day, hour, minute, second)
    val date2 = calendar.getTime
    calendar.set(year, month, day+1, hour+1, minute, second)
    val date3 = calendar.getTime

    val idNonValue = 0
    val post1 = StatusImpl("Post1", date1, idNonValue, idNonValue, null)
    val post2 = StatusImpl("Post2", date2, idNonValue, idNonValue, null)
    val post3 = StatusImpl("Post3", date3, idNonValue, idNonValue, null)
    Seq(post1, post2, post3)
  }

  private def createTestingStringArrayList: util.ArrayList[String] = {
    val post1 = "texto pos 0\ttexto pos 1\ttexto pos 2\t2020-01-03\t12:00:00\tIbaiLlanos\tSkainLoL\t26\t69\t2791" +
      "\t\"buen cochazo se va notando el sueldo de caster\"\t\t\t\t\t1303310580464398336" +
      "\thttps://twitter.com/IbaiLlanos/status/1303310580464398336\t\t\t\t\t\t\t\t\t\t\t\t\t\t\treplyUser"
    val post2 = "texto pos 0\ttexto pos 1\ttexto pos 2\t2020-01-01\t10:00:00\tIbaiLlanos\t\t65\t728\t8380\t\"Lo sabía." +
      " Lo sabía desde hace tiempo, y Jordi Cruz su ayudante.Qué hijos de puta, como se lo tenían callado." +
      "http://ver.20m.es/jibeb3\"\t\t\t\t1303307843391684609\thttps://twitter.com/IbaiLlanos/status/1303307843391684609" +
      "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\treplyUser"
    val post3 = "texto pos 0\ttexto pos 1\ttexto pos 2\t2020-01-01\t10:0:00\tIbaiLlanos\tjxtamartin\t53\t53\t4683\t" +
      "\"No os voy a invitar a cenar eh\"\t\t\t\t1303264091662880769\thttps://twitter.com/IbaiLlanos/status/1303264091662880769" +
      "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\treplyUser"
    val arrayListToReturn = new util.ArrayList[String]()
    arrayListToReturn.add(post1)
    arrayListToReturn.add(post2)
    arrayListToReturn.add(post3)

    arrayListToReturn
  }
  @Test
  def groupTwitterActionsByDatesTest(): Unit = {
    val grouped = groupTwitterActionsByDates(postSeq, postArrayList)
    val differentDatesPosts = 2
    assertEquals(differentDatesPosts, grouped.size )
    logger.debug("asdad")
  }
}
