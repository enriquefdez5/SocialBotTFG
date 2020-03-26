package model

import model.Plataforma.Plataforma
import twitter4j.GeoLocation

class Post(val id: Long, val user: User, val text: String, val createdAt: String, val retweetsCount: Int,
           val favouritesCount: Int, val geoLocation: GeoLocation, val isRetweeted: Boolean,
           val isFavourited: Boolean, val plataforma: Plataforma) {

  override def toString() : String = {
    "[id : " + id +
      ", text = " + text +
      ", createdAt = " + createdAt +
      ", retweetsCount = " + retweetsCount +
      ", favouritesCount = " + favouritesCount +
      ", geoLocation = " + geoLocation +
      ", isRetweeted = " + isRetweeted +
      ", isFavourited = " + isFavourited +
      ", plataforma = " + plataforma +
      ", user = " + user + "]"

  }
}
