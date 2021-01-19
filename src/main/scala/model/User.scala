package model

/**
 * Data class with info about a user(twitter user at the moment).
 *
 * @param id Int, Value for user id. ie:(321321)
 * @param strId String, id value for user. Ej: ("321321")
 * @param profileImg ProfileImg, info about profile image
 */
case class User(id: Long, strId: String, userInfo: UserInfo, userStats: UserStats, profileImg: ProfileImg)


// ---------------- Probably never will be used ------------------- //
/**
 * Data class with profile info
 * @param name String, User's profile name. Ej: ("Ibai")
 * @param location String, Nullable, the user-defined location for this account’s profile, it could have weird format not
 *                 machine-parseable
 * @param profileUrl String, User's profile Url
 * @param description String, User's profile description
 * @param createdAt String, profile's creation date
 */
case class UserInfo(name: String, location: String, profileUrl: String, description: String, createdAt: String )

/**
 * Data class with profile stadistics
 * @param isProtected Boolean, if it is a protected account or not.
 * @param isVerified Boolean, if it is a verified account or not.
 * @param followers Int, number of followers the user has.
 * @param followings Int, number accounts user is following.
 */
case class UserStats(isProtected: Boolean, isVerified: Boolean, followers: Int, followings: Int)

/**
 * Data class with profile image info
 * @param defaultProfileImgUrl Boolean, if true the user has not updated his profile image
 * @param profileImgUrl String, profile image url
 */
case class ProfileImg(defaultProfileImgUrl: Boolean, profileImgUrl: String)
