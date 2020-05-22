package ex.Model

import io.circe.generic.semiauto._


final case class User(id:Int, name: String, email:String)

object User {
        implicit def jsonEncoder = deriveEncoder[User]
        implicit def JsonDecoder = deriveDecoder[User]
}


