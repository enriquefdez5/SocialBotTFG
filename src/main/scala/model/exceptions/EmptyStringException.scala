package model.exceptions

/** Exception for empty string value.
 *
 * @constructor Create a new empty string exception with a message.
 * @param msg. Message to throw.
 */
case class EmptyStringException(msg: String = "") extends AIException
