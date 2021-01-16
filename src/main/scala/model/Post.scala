package model

// model
import java.util.Date

import Plataforma.Plataforma
import twitter4j.Status
//import .Source

// twitter4j
//import twitter4j.GeoLocation

//case class Post(id: Long, user: User, text: String, createdAt: String, retweetsCount: Int,
//           favouritesCount: Int, geoLocation: GeoLocation, isRetweeted: Boolean,
//           isFavourited: Boolean, plataforma: Plataforma)
case class Post(text: String, createdAt: Date, retweetedStatus: Status, getInReplyToUserId: Long)
