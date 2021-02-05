package twitterapi.commandActions

import utilities.ConfigRun

trait ActionCommand {

  val value: Int
  def execute(conf: ConfigRun): Unit

}
