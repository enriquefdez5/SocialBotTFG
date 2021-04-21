package app.actionExecution

case class ActionExecutionData(twitterUsername: String, twitterUsernameWhereToPost: String,
                               sameHourCount: Int, meanActionsPerHour: Int,
                               followedPostActionsCount: Int, maxFollowedPostActions: Int)
