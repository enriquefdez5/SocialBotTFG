package twitterapiTest

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertTrue}

import java.util.Date

import app.twitterAPI.{ConfigRun, TwitterClientTrait}

import app.twitterAPI.TwitterService.{getTweets, getTwintTweets}
import app.twitterAPI.commandActions.{PostCommand, ReplyCommand, RtCommand}
import model.exceptions.IncorrectSizeListException

import utilities.dates.DatesUtilTrait
import utilities.fileManagement.FileReaderUtilTrait

class TwitterServiceTest extends FileReaderUtilTrait with DatesUtilTrait with TwitterClientTrait {

  val emptyListExceptionMessage = "List can not be empty"

  private def getConfItem(): ConfigRun = {
    val arraySize = 8
    val args = new Array[String](arraySize)
    val consumerKeyOption = "--consumer-token-key"
    val consumerKey = "5JUmI2nlOFYhvKrWQJrK3DcNC"
    val consumerKeySecretOption = "--consumer-token-key-secret"
    val consumerKeySecret = "XKsFu2p5xEsf4NfSS6JSU7po2MPv3TqwHKw8MKZLpgcDV582zB"
    val accessKeyOption = "--access-token-key"
    val accessKey = "1226181568454066181-1Pq0Xpe82I2amKhmydBmsCSfr8Oqd2"
    val accessKeySecretOption = "--access-token-key-secret"
    val accessKeySecret = "GkLucdYD3LWQK10C7NvywGYFA5PzNiH6jLPRqNPdQLPMm"
    args(0) = consumerKeyOption
    args(1) = consumerKey
    args(2) = consumerKeySecretOption
    args(3) = consumerKeySecret
    args(4) = accessKeyOption
    args(5) = accessKey
    args(6) = accessKeySecretOption
    args(7) = accessKeySecret
    new ConfigRun(args)
  }


//  @Test
//  def getTwintTweetsTest(): Unit = {
//    val username = "lec"
//    val csvFilePath = "./getTwintTweetsTest"
//    val selectedOption = 1
//    // Selected option 1
//    getTwintTweets(username, csvFilePath, selectedOption)
//    val data = readCSVFile(csvFilePath + username + ".csv")
//    // It is not null
//    assertNotNull(data)
//    // It has some data in it
//    assertTrue(data.size() > 10)
//
//    // Selected option 2
//      // valid before date
//    val dayOfWeek = 30
//    val hourOfDay = 23
//    val month = 3
//    val year = 2021
//    val date = buildDate(dayOfWeek, hourOfDay, month, year)
//    val secondCSVPath = "./getTwintTweetsTest2"
//    val csvPattern = "yyyy-MM-dd HH:mm:ss"
//
//    // Build date formats
//    val simpleDateFormat = getSimpleDateFormat(csvPattern)
//
//    getTwintTweets(username, secondCSVPath, selectedOption + 1, date)
//    val data2 = readCSVFile(secondCSVPath + username + ".csv")
//
//    // It is not null
//    assertNotNull(data2)
//    // It has some data in it
//    assertTrue(data2.size() > 0)
//    val lastElement = data2.get(data2.size()-1)
//    val dateString = lastElement.split("\t")(2)
//    // Pattern for csv file dates
//
//    val parsedDate = simpleDateFormat.parse(dateString)
//    assertTrue(parsedDate.after(date))
//
//      // After date
//    val newYear = 2025
//    val afterDate = buildDate(dayOfWeek, hourOfDay, month, newYear)
//    val thirdCSVPath = "./getTwintTweetsTest3"
//    getTwintTweets(username, thirdCSVPath, selectedOption + 1, afterDate)
//    val data3 = readCSVFile(thirdCSVPath + username + ".csv")
//    assertEquals(0, data3.size())
//
//  }

//  @Test
//  def getTweetsTest(): Unit = {
//    val twitterConf: ConfigRun = getConfItem()
//    val username = "lec"
//
//    // option 0
//    val tweets = getTweets(twitterConf, username)
//    assertTrue(tweets.nonEmpty)
//
//    // option 1
//      // date after
//    val dayOfWeek = 30
//    val hourOfDay = 23
//    val month = 3
//    val yearAfter = 2030
//    val date = buildDate(dayOfWeek, hourOfDay, month, yearAfter)
//    try {
//      getTweets(twitterConf, username, 2, date)
//    }
//    catch {
//      case exception: IncorrectSizeListException => assertEquals(emptyListExceptionMessage, exception.msg)
//    }
//
//      // date before
//    val year = 2021
//    val date2 = buildDate(dayOfWeek, hourOfDay, month, year)
//
//    val tweets3 = getTweets(twitterConf, username, 2, date2)
//    assertTrue(tweets3.nonEmpty)
//    assertTrue(tweets3.last.createdAtDate.after(date2))
//  }


  @Test
  def executeActionTest(): Unit = {
    val twitterConf: ConfigRun = getConfItem()
    val twitterUsername = "testModel"


    // post action
    val currentDate = new Date()
    val postCommand = new PostCommand()
    postCommand.execute(twitterUsername, twitterConf)
    val twitter = getTwitterClient(twitterConf)
    logger.info((twitter.getHomeTimeline().get(0).getCreatedAt.compareTo(currentDate) >= 0).toString)
    assertTrue(twitter.getHomeTimeline().get(0).getCreatedAt.compareTo(currentDate) >= 0)

    // Tests not executed to not disturb Twitter users who gets their tweets retweeted or replied.
//    // rtAction
//    val retweetCommand = new RtCommand()
//    retweetCommand.execute(twitterUsername, twitterConf)
//    assertTrue(twitter.getHomeTimeline().get(0).isRetweeted)
//
//    // reply action
//    val replyCommand = new ReplyCommand()
//    replyCommand.execute(twitterUsername, twitterConf)
//    assertTrue(twitter.getHomeTimeline().get(0).getInReplyToUserId != 0)
  }


}
