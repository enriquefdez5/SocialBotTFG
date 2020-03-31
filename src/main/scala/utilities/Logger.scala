package utilities

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.scala.Logging

object Logger extends Logging {

  def log( level: Level, msg: String): Unit ={
    logger(level, msg)
  }
}
