package app.twitterAPI.commandActions

import app.twitterAPI.ConfigRun

/** Trait for command pattern. It has a value and an execute function. */
trait ActionCommandTrait {

  val value: Int

  /** Execute an action.
   *
   * @param twitterUsername. Twitter username.
   * @param conf. Item built with main args to interact with Twitter API.
   */
  def execute(twitterUsername: String, conf: ConfigRun): Unit

}
