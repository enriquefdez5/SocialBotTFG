package model.exceptions

/** Exception for an incorrect size list value.
 *
 * @constructor Create a new incorrect size list exception with a message.
 * @param msg. Message to throw.
 */
case class IncorrectSizeListException(msg: String = "") extends AIException
