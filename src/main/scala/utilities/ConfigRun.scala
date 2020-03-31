package utilities

import org.rogach.scallop.{ScallopConf, ScallopOption}

class ConfigRun(arguments: Seq[String]) extends ScallopConf(arguments) {
  val consumerTokenKey: ScallopOption[String] = opt[String](required = true)
  val consumerTokenKeySecret: ScallopOption[String] = opt[String](required = true)
  val accessTokenKey: ScallopOption[String] = opt[String](required = true)
  val accessTokenKeySecret: ScallopOption[String] = opt[String](required = true)
  verify()
}
