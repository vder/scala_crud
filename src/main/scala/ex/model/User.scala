package ex.Model

import cats.Applicative
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.circe._
import io.circe.generic.auto._
import cats.effect.Sync


final case class User(id:Int, name: String, email:String)

object User {
        implicit def encodeUser[A[_]: Applicative]: EntityEncoder[A, User] = jsonEncoderOf[A,User]
        implicit def userDecoder[A[_]: Sync] = jsonOf[A, User]
}


