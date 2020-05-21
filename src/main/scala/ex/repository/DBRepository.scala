package ex.repository

import ex.Model.User
import doobie.util.transactor.Transactor
import doobie.implicits._
import cats.effect.Bracket
import cats.effect.Sync

trait DBRepository[F[_]] {
  def getUser(id: Int): F[Option[User]]
  def deleteUser(id: Int): F[Int]
  def updateUser(user: User): F[User]
  def insertUser(user: User): F[User]
}

class LiveDBRepository[F[_]] private (val xa: Transactor[F])(
    implicit br: Bracket[F, Throwable]
) extends DBRepository[F] {

  override def getUser(id: Int): F[Option[User]] =
    sql"select * from users where id =$id".query[User].option.transact(xa)

  override def deleteUser(id: Int): F[Int] =
    sql"delete from users where id=$id".update.run.transact(xa)

  override def updateUser(user: User): F[User] =
    sql"update users set name =${user.name}, email =  ${user.email} where id = ${user.id}".update
      .withUniqueGeneratedKeys[User]("id", "name", "email")
      .transact(xa)

  override def insertUser(user: User): F[User] =
    sql"insert into users (id,name, email) select ${user.id},${user.name}, ${user.email} where not exists (select null from users where id = ${user.id})".update
      .withUniqueGeneratedKeys[User]("id", "name", "email")
      .transact(xa)

}

object LiveDBRepository {
  def make[F[_]: Sync](xa: Transactor[F]) = Sync[F].delay {
    new LiveDBRepository[F](xa)
  }
}
