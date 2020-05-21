import cats.effect._
import doobie.hikari._
import doobie.util.ExecutionContexts
import doobie._
import doobie.implicits._
import cats.implicits._
import doobie.util.ExecutionContexts



val z = "23432"

s" test $z"


List(1,2,3,4)
.map(_+3)
.map(_*8)




// We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
// is where nonblocking operations will be executed. For testing here we're using a synchronous EC.
implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

 val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      be <- Blocker[IO] // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver", // driver classname
        "jdbc:postgresql:192.168.99.100:54340/doobie", // connect URL
        "vder", // username
        "gordon", // password
        ce, // await connection here
        be // execute JDBC operations here
      )
    } yield xa



   val program1 = 42.pure[ConnectionIO]




val x= "32"

   
   