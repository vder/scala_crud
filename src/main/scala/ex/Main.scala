package ex

import cats.effect._
import org.http4s.server.blaze._
import doobie.hikari._
import doobie.util.ExecutionContexts
import ex.controllers.LiveUserController
import ex.repository.LiveDBRepository
import org.http4s.implicits._

object Main extends IOApp {

  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      be <- Blocker[IO] // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver", // driver classname
        "jdbc:postgresql://192.168.99.100:54340/doobie", // connect URL
        "vder", // username
        "gordon", // password
        ce, // await connection here
        be // execute JDBC operations here
      )
    } yield xa

  override def run(args: List[String]): IO[ExitCode] =
    transactor
      .use { xa =>
        for {
          dbRepo <- LiveDBRepository.make(xa)
          controller <- LiveUserController.make(dbRepo)
          routes <- controller.getRoutes
          stop <- BlazeServerBuilder[IO]
            .bindHttp(9000, "localhost")
            .withHttpApp(routes.orNotFound)
            .withNio2(true)
            .resource
            .use(_ => IO.never)
            .as(ExitCode.Success)
        } yield stop
      }

}
