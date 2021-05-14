package model.exceptions

/** Exception for a wrong parameter value.
 *
 * @constructor Create a new wrong param value exception with a message.
 * @param msg. Message to throw.
 */
case class WrongParamValueException(msg: String) extends AIException
