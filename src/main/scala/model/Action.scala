package model

/**
 *  Enumeration object containing the three types of Actions available.
 */

object Action extends Enumeration {
  type Action = Value
  val POST, RT, REPLY = Value

  def getActionFromIntValue(value: Int): Action = {
    value match {
      case 1 => POST
      case 2 => RT
      case 3 => REPLY
    }
  }
}
