import utilities.console.ConsoleUtilTrait

/**
 * Object with main method for system execution.
 */
object MainManager extends ConsoleUtilTrait {
  /** Main method of the system.
   *
   * @param args. Args to build item to interact with Twitter API.
   */
  def main(args: Array[String]): Unit = {
    showMainMenuOptions(args)
  }
}

