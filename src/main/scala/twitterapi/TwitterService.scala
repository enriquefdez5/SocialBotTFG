package twitterapi

// util libs
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date, Locale, Properties}

import utilities.{ConfigRun, IOUtil}
import org.apache.logging.log4j.scala.Logging
import twitterapi.TwitterFilter.obtainRts

// scala libs
import scala.collection.JavaConversions._
import scala.annotation.tailrec

// app libs
import model.{Post, User, UserInfo, UserStats, ProfileImg}

// twitter4j libs
import twitter4j.{Paging, Status, TwitterFactory, Twitter}
import twitter4j.conf.ConfigurationBuilder

object TwitterService extends Logging {

  // Read properties file
  val properties: Properties = new Properties()
  properties.load(new FileInputStream("src/main/resources/config.properties"))

  val csvSeparator = ","
  /**
   * Method that obtains tweets from csv file and from twitter account.
   * This method transform those tweets into training data with 3 colums.
   * First column is an integer that represents the day of the week the tweet was posted
   * Second column is an integer that represents the hour of the day the tweet was posted
   * Third column is an integer that represents the type of action it was the tweet
   *  (1 for tweets, 2 for replys and 3 for rtweets)
   * @param tweets. Tweets colected from twitter account
   * @return  Data as a Seq of String. Each entry represents an action on twitter with info of that action
   */
  def obtainActions(tweets: Seq[Post]): Seq[String] = {
    // Read csv and remove first row
    val csvTweets: util.ArrayList[String] = IOUtil.readCSVFile()
    csvTweets.remove(0)

    // Pattern for csv file dates
    val csvPattern = "yyyy-MM-dd HH:mm:ss"
    // Pattern for twitter dates
    val apiPattern = "EEE MMM dd HH:mm:ss z yyyy"
    // Build date formats
    val simpleDateFormatCSV = new SimpleDateFormat(csvPattern)
    val simpleDateFormatAPI = new SimpleDateFormat(apiPattern, Locale.ENGLISH)
    // Get calendar instance
    val calendar = Calendar.getInstance()

    // Get time from tweets read in csv file
    val csvDates = csvTweets.map(line => {
      val split = line.split(csvSeparator)
      calendar.setTime(simpleDateFormatCSV.parse(split(0)))
      calendar.getTime.toString + ", " + split(2)
    })

    // Filter rts from tweets collected from twitter api
    val rts = obtainRts(tweets)
    // Get date from those rts
    val rtsDates: Seq[String] = rts.map(rt => {
      val stringToReturn = rt.toString() + ",3"
      stringToReturn
    })

    val dates = csvDates.map( csvDate => {
      csvDate
    })

    // Mix twitter api and csv tweets dates
    rtsDates.foreach(rt => {
      dates.add(rt)
    })

    val calendarToOrder = Calendar.getInstance()
    val tweetsToMixOrdered = dates.sortBy(tweet => {
      val stringToDate = tweet.split(csvSeparator)(0)
      calendarToOrder.setTime(simpleDateFormatAPI.parse(stringToDate))
      calendarToOrder.getTime
    })

    var lastCalendarDay: Int = 8

    val tweetsToReturn = tweetsToMixOrdered.map(entry => {
      val splitEntry = entry.split(csvSeparator)
      calendar.setTime(simpleDateFormatAPI.parse(splitEntry(0)))
      val stringToReturn: StringBuilder = new StringBuilder("")

      // Check if it is a new week
      if (calendar.get(Calendar.DAY_OF_WEEK) < lastCalendarDay) {
        stringToReturn.append("-1\n")
      }

      // Check interaction type (Post, Reply...)
      if (splitEntry(1) == " ") {
        stringToReturn.append(calendar.get(Calendar.DAY_OF_WEEK) +
          csvSeparator + calendar.get(Calendar.HOUR_OF_DAY) + csvSeparator + "1\n")

      }
      if (splitEntry(1) == "3") {
        stringToReturn.append(calendar.get(Calendar.DAY_OF_WEEK) +
          csvSeparator + calendar.get(Calendar.HOUR_OF_DAY) + csvSeparator + "3\n")
      }
      else {
        stringToReturn.append(calendar.get(Calendar.DAY_OF_WEEK) +
          csvSeparator + calendar.get(Calendar.HOUR_OF_DAY) + csvSeparator + "2\n")
      }
      lastCalendarDay = calendar.get(Calendar.DAY_OF_WEEK)

      stringToReturn.toString()
    })
    tweetsToReturn
  }

  def obtainTweetInfo(): Seq[String] = {
    val dates = readCSVDates()

    // Calculate date time since first day of the month
    val orderedTimeValues: util.List[Long] = new util.ArrayList[Long]()
    val date = new Date()
    val firstDayOfMonthDate = getFirstDayOfMonth(date)
    val idx = 0
    calculateTimeValues(orderedTimeValues, idx, dates, firstDayOfMonthDate  )

    orderedTimeValues.map(_.toString + "\n")
  }

  private def readCSVDates(): Seq[Date] = {
    val tweetInfo: util.ArrayList[String] = IOUtil.readCSVFile()
    // Delete header row
    tweetInfo.remove(0)

    val filteredTweets = tweetInfo.filter(tweetElement => {
      tweetElement.split(csvSeparator)(2) == ""
    })

    val pattern = "yyyy-MM-dd HH:mm:ss"
    val simpleDateFormat = new SimpleDateFormat(pattern)
    val splitDate = filteredTweets.map(_.split(csvSeparator)(0))
    splitDate.map(date => {
      simpleDateFormat.parse(date)
    })
  }


  @tailrec
  private def calculateTimeValues(orderedTimeValues: util.List[Long],
                                  idx: Int, dates: util.List[Date],
                                  firstDayOfMonthParam: Date) : util.List[Long] = {
    if (idx < dates.length) {
      val idxDate = dates(idx)
      val dateFirstDayOfMonth: Date = getFirstDayOfMonth(idxDate)
      val roundedDateFirstDayOfMonth = (dateFirstDayOfMonth.getTime / 1000).toInt
      val roundedDateFirstDayOfMonthParam = (firstDayOfMonthParam.getTime / 1000).toInt

      if (roundedDateFirstDayOfMonth.equals(roundedDateFirstDayOfMonthParam)) {
        val subDates = Math.subtractExact(idxDate.getTime, dateFirstDayOfMonth.getTime)
        orderedTimeValues.add(subDates)
        calculateTimeValues(orderedTimeValues, idx + 1, dates, dateFirstDayOfMonth)
      }
      else {
        val firstDayOfPreviousMonth = dateFirstDayOfMonth
        orderedTimeValues.add(-1)
        calculateTimeValues(orderedTimeValues, idx, dates, firstDayOfPreviousMonth)
      }
    }
    else {
      orderedTimeValues
    }
  }

  def getFirstDayOfMonth(date: Date): Date = {
    val calendar = Calendar.getInstance()
    calendar.setTime(date)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.getTime
  }


  def getTweets(conf: ConfigRun, userName: String): Seq[Post] = {
    val pagInit = 1

    // Obtain twitter client
    val twitter = getTwitterClient(conf)

    // Get profile data
    val userSearched = twitter.showUser(userName)
    logger.debug(s"Showing $userName profile Info")

    // Create profile just in case I need it for NN or something
    val user = createUser(userSearched)

    logger.debug(userSearched.toString)

    // Get ~3200 user tweets
    val tweets = gatherTweets(twitter, pagInit, userName, user, Seq())
    //    tweets.map(tweet => Post(tweet.getId, user, tweet.getText, tweet.getCreatedAt.toString, tweet.getRetweetCount,
    //      tweet.getFavoriteCount, tweet.getGeoLocation, tweet.isRetweeted, tweet.isFavorited, Plataforma.twitter))
    tweets.map(tweet => {
      val calendar = Calendar.getInstance()
      calendar.setTime(tweet.getCreatedAt)
      Post(tweet.getText, calendar.getTime, tweet.getRetweetedStatus,
        tweet.getInReplyToStatusId)
    })
  }

  /**
   * Function for collecting tweets
   *
   * @param twitter  , client to request operation
   * @param pageInit , number of page from which tweets are collected
   * @param userName , user profile where to search
   * @param user     , app object that represents a user profile
   * @param tweets   , list of tweets collected
   */
  @tailrec
  private def gatherTweets(twitter: Twitter, pageInit: Int, userName: String, user: User,
                                 tweets: Seq[Status]): Seq[Status] = {
    if (tweets.size < properties.getProperty("maxNumberTweetsAllowed").toInt) {
      val page = new Paging(pageInit, properties.getProperty("gatheringTweetsPageSize").toInt)
      val newTweets: Seq[Status] = twitter.getUserTimeline(userName, page).toSeq
      logger.debug(s"Gathered ${newTweets.size()} tweets")
      gatherTweets(twitter, pageInit + 1, userName, user, tweets ++ newTweets)
    }
    else {
      tweets
    }
  }


  /**
   * Method for creating a twitter client instane
   *
   * @return Twitter, a twitter client instance
   */
  private def getTwitterClient(conf: ConfigRun): Twitter = {
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey(conf.consumerTokenKey())
      .setOAuthConsumerSecret(conf.consumerTokenKeySecret())
      .setOAuthAccessToken(conf.accessTokenKey())
      .setOAuthAccessTokenSecret(conf.accessTokenKeySecret())
    val tf = new TwitterFactory(cb.build)
    val twitter = tf.getInstance
    twitter
  }

    /**
     * Method for creating an object with user info needed
     *
     * @param userSearched with info needed to create the app User
     * @return User, app user object
     */
    private def createUser(userSearched: twitter4j.User): User = User(id = userSearched.getId,
    strId = userSearched.getId.toString,
      userInfo = UserInfo(name = userSearched.getName,
        description = userSearched.getDescription, createdAt = userSearched.getCreatedAt.toString,
        location = userSearched.getLocation, profileUrl = userSearched.getURL),
      userStats = UserStats(followers = userSearched.getFollowersCount, followings = userSearched.getFriendsCount,
        isProtected = userSearched.isProtected, isVerified = userSearched.isVerified),
      profileImg = ProfileImg(defaultProfileImgUrl = userSearched.isDefaultProfileImage,
        profileImgUrl = userSearched.getProfileImageURL)
    )

}







