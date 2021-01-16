package model

object Action extends Enumeration {
  type Action = Value

  // Assigning values
  val rt: Value = Value("Rt")
  val post: Value = Value("Post")
  val reply: Value = Value("Reply")
}
