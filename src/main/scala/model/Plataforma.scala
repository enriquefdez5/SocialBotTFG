package model

/**
 * Enumeration object for social network platform
 */
object Plataforma extends Enumeration {
  type Plataforma = Value

  // Assigning values
  val twitter: Value = Value("Twitter")
  val facebook: Value = Value("Facebook")
  // Possible social network for obtaining data
  val whatsapp: Value = Value("WhatsApp?")
  val instagram: Value = Value("Instagram?")

}
