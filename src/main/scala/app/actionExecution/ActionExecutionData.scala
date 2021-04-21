package app.actionExecution

case class ActionExecutionData(twitterUsername: String,
                               sameHourCount: Int, meanActionsPerHour: Int,
                               followedPostActionsCount: Int, maxFollowedPostActions: Int)
