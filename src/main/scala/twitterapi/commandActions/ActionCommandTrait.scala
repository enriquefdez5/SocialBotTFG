package twitterapi.commandActions

import utilities.ConfigRun

trait ActionCommandTrait {

  val value: Int
  def execute(conf: ConfigRun): Unit

}
