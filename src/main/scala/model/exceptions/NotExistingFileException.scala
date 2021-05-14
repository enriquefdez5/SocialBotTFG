package model.exceptions

/** Exception for a not existing file.
 *
 * @constructor Create a new not existing file exception with a message.
 * @param msg. Message to throw.
 */
case class NotExistingFileException(msg: String = "") extends AIException
