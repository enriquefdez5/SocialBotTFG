package twitterapi.commandActions

import utilities.ConfigRun

trait ActionCommand {

  def execute(conf: ConfigRun): Unit

  def getValue(): Int
}
