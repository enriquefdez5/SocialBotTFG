package model.exceptions

/** Exception for an invalid action.
 *
 * @constructor Create a new invalid action exception with a message.
 * @param msg. Message to throw.
 */
case class InvalidActionException(msg: String) extends AIException
