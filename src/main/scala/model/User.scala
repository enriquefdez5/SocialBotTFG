package model

/**
 * Clase que representa a un usuario de twitter
 * @param id Int, Value for user id. ie:(321321)
 * @param strId String, id value for user. Ej: ("321321")
 * @param name String, User's profile name. Ej: ("Ibai")
 * @param location String, Nullable, the user-defined location for this account’s profile, it could have weird format not
 *                 machine-parseable
 * @param profileUrl String, User's profile Url
 * @param description String, User's profile description
 * @param isProtected Boolean, if it is a protected account or not.
 * @param isVerified Boolean, if it is a verified account or not.
 * @param followers Int, number of followers the user has
 * @param followings Int, number accounts user is following.
 * @param createdAt String, profile's creation date
 * @param defaultProfileImgUrl Boolean, if true the user has not updated his profile image
 * @param profileImgUrl String, profile image url
 */
class User(var id:Long, var strId:String, var name:String, var location:String, var profileUrl:String,
           var description:String, var isProtected:Boolean, var isVerified:Boolean, var followers:Int,
           var followings:Int, var createdAt:String, var defaultProfileImgUrl:Boolean, var profileImgUrl:String ) {
  override def toString() : String = {
     "[id : " + id +
      ", strId = " + strId +
      ", name = " + name +
      ", description = " + description +
      ", profileUrl = " + profileUrl +
      ", location = " + location +
      ", isProtected = " + isProtected +
      ", isVerified = " + isVerified +
      ", followers = " + followers +
      ", followings = " + followings +
      ", createdAt = " + createdAt +
      ", defaultProfileImgUrl = " + defaultProfileImgUrl +
      ", profileImgUrl = " + profileImgUrl + "]";

  }

}
