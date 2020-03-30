package model

//model
import Plataforma.Plataforma

//twitter4j
import twitter4j.GeoLocation

case class Post(id: Long, user: User, text: String, createdAt: String, retweetsCount: Int,
           favouritesCount: Int, geoLocation: GeoLocation, isRetweeted: Boolean,
           isFavourited: Boolean, plataforma: Plataforma)
