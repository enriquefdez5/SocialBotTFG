package model.exceptions

/** Exception for an incorrect command action value.
 *
 * @constructor Create a new incorrect command action exception with a message.
 * @param msg. Message to throw.
 */
case class IncorrectCommandActionException(msg: String = "") extends AIException
