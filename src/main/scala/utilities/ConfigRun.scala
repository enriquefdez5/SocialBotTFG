package utilities

import org.rogach.scallop.ScallopConf

class ConfigRun(arguments: Seq[String]) extends ScallopConf(arguments){
  val consumerTokenKey = opt[String](required = true)
  val consumerTokenKeySecret = opt[String](required = true)
  val accessTokenKey = opt[String](required = true)
  val accessTokenKeySecret = opt[String](required = true)
  verify()
}
