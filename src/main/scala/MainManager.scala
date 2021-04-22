import app.actionExecution.MainActionExecution
import app.dataRecovery.MainDataRecovery
import neuralNetworks.rnnActionGenerator.MainNNActionGenerator
import neuralNetworks.rnnCharacterGenerator.MainNNCharacterGenerator
import org.apache.logging.log4j.scala.Logging

import scala.io.StdIn

class MainManager extends Logging {

  def showOptions(): Unit = {
    logger.info("\nWhat do you want to do? \n" +
      "1. Recover user data\n" +
      "2. Train neural network for text generation\n" +
      "3. Train neural network for action generation\n" +
      "4. Execute actions\n" +
      "0. Exit\n")
  }

  def readOption(args: Array[String]): Unit = {
    StdIn.readInt() match {
      case 1 => MainDataRecovery.main(args)

      case 2 => MainNNCharacterGenerator.main(args)

      case 3 => MainNNActionGenerator.main(args)

      case 4 => MainActionExecution.main(args)

      case 0 => System.exit(1)

      case _ =>
        logger.info("Not a valid option, try again.")
        readOption(args)
    }
  }
}

object MainManager extends Logging {
  def main(args: Array[String]): Unit = {
    val mainManager: MainManager = new MainManager()
    mainManager.showOptions()
    mainManager.readOption(args)
  }
}

