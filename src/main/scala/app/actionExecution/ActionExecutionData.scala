package app.actionExecution

/** Case class with parameters for action execution.
 *
 * @constructor create a new ActionExecutionData case class.
 * @param twitterUsername. Twitter username.
 * @param sameHourCount. Current number of post at same hour.
 * @param meanActionsPerHour. Mean actions per hour.
 * @param followedPostActionsCount. Followed post actions.
 * @param maxFollowedPostActions. Number of maximum followed post actions.
 */
case class ActionExecutionData(twitterUsername: String,
                               sameHourCount: Int, meanActionsPerHour: Int,
                               followedPostActionsCount: Int, maxFollowedPostActions: Int)
