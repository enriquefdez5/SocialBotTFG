package model

/**
 * Enumeration object for social network platform
 */
object Plataforma extends Enumeration {
  type Plataforma = Value

  // Assigning values
  val twitter = Value("Twitter")
  val facebook = Value("Facebook")
  // Possible social network for obtaining data
  val whatsapp = Value("WhatsApp?")
  val instagram = Value("Instagram?")

}
