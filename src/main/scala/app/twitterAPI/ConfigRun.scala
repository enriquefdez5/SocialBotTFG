package app.twitterAPI

import org.rogach.scallop.{ScallopConf, ScallopOption}

/** Class that contains tokens and keys to access Twitter API.
 *
 * @constructor Create a new configuration for the run with the required arguments
 * @param arguments. Main args.
 */
class ConfigRun(arguments: Seq[String]) extends ScallopConf(arguments) {
  val consumerTokenKey: ScallopOption[String] = opt[String](required = true)
  val consumerTokenKeySecret: ScallopOption[String] = opt[String](required = true)
  val accessTokenKey: ScallopOption[String] = opt[String](required = true)
  val accessTokenKeySecret: ScallopOption[String] = opt[String](required = true)
  verify()
}
