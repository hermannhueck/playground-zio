package overview.ch07runningeffects

import java.io.IOException

import zio.ZIO
import zio.console._

import util.formatting._

/*
  type UIO[+A]       = ZIO[Any, Nothing, A]
  type URIO[-R, +A]  = ZIO[R, Nothing, A]
  type Task[+A]      = ZIO[Any, Throwable, A]
  type RIO[-R, +A]   = ZIO[R, Throwable, A]
  type IO[+E, +A]    = ZIO[Any, E, A]
 */
object MyZioApp extends zio.App {

  override def run(args: List[String]): ZIO[Console, Nothing, Int] =
    myAppLogic.fold(_ => 1, _ => 0)

  val myAppLogic: ZIO[Console, IOException, Unit] =
    for {
      _ <- putStrLn(header(objectName(this)))
      _ <- putStrLn(textInLine("Running Effects"))
      _ <- putStrLn("Hello! What is your name?")
      name <- getStrLn
      _ <- putStrLn(s"Hello, ${name}, welcome to ZIO!")
      _ <- putStrLn(line())
    } yield ()
}
