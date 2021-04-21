package app.twitterAPI.commandActions

import app.twitterAPI.ConfigRun

trait ActionCommandTrait {

  val value: Int
  def execute(twitterUsername: String, conf: ConfigRun): Unit

}
