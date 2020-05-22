package ex.controllers

import org.http4s.HttpRoutes
import org.http4s._
import ex.Model.User
import org.http4s.dsl.Http4sDsl
import cats.Applicative
import cats.Defer
import org.http4s.circe.jsonEncoderOf
import org.http4s.circe._
import ex.repository.DBRepository
import cats.Monad
import cats.effect.Sync
import cats.Functor
import cats.implicits._
import io.circe._
import io.circe.syntax._

trait UserController[F[_]] {
  def getRoutes: F[HttpRoutes[F]]

  def insertUser(user: User): F[Int]
  def updateUser(user: User): F[Int]
  def getUser(userId: Int): F[Response[F]]
  def deleteUser(userId: Int): F[Int]

}

class LiveUserController[F[_]: Applicative: Defer: Monad: Sync: Functor](
    dbRepo: DBRepository[F]
) extends UserController[F]
    with Http4sDsl[F] {

  override def getRoutes = {

    implicit def encodeInt: EntityEncoder[F, Int] =
      jsonEncoderOf

    Sync[F].delay {
      HttpRoutes.of[F] {
        case GET -> Root / "user" / id =>
          getUser(id.toInt)
        case DELETE -> Root / "user" / id =>
          Ok(deleteUser(id.toInt))
        case req @ POST -> Root / "user" =>
          req.decode[Json] { json =>
            json.as[User] match {
              case Right(usr) => Ok(insertUser(usr))
              case _          => BadRequest()
            }
          }
        case req @ PUT -> Root / "user" =>
          req.decode[Json] { json =>
            json.as[User] match {
              case Right(user) => Ok(updateUser(user))
              case _           => BadRequest()
            }
          }
      }
    }
  }

  override def insertUser(user: User): F[Int] =
    dbRepo.insertUser(user).map(_.id)

  override def updateUser(user: User): F[Int] =
    dbRepo.updateUser(user).map(_.id)

  override def getUser(userId: Int): F[Response[F]] =
    dbRepo.getUser(userId).flatMap {
      case Some(x) => Ok(x.asJson)
      case None    => NotFound()
    }

  override def deleteUser(userId: Int): F[Int] = dbRepo.deleteUser(userId)

}

object LiveUserController {
  def make[F[_]: Sync](dbRepo: DBRepository[F]) = Sync[F].delay {
    new LiveUserController[F](dbRepo)
  }
}
