package overview.ch01creatingeffects

import java.io.IOException
import java.net.{ServerSocket, Socket}

import zio.{DefaultRuntime, IO, Task, UIO, ZIO}

import scala.util.chaining._
import scala.io.{Codec, Source, StdIn}
import scala.util.Try

import util.formatting._

/*
  type UIO[+A]       = ZIO[Any, Nothing, A]
  type URIO[-R, +A]  = ZIO[R, Nothing, A]
  type Task[+A]      = ZIO[Any, Throwable, A]
  type RIO[-R, +A]   = ZIO[R, Throwable, A]
  type IO[+E, +A]    = ZIO[Any, E, A]
 */
object CreatingEffects extends App {

  // ------------------------------------------------------------
  printHeaderWithProgramName(this)

  val runtime = new DefaultRuntime {}

  // ------------------------------------------------------------
  printTextInLine("From Success Values: .succeed, .effectTotal")

  val s1: UIO[Int] = ZIO.succeed(42)
  val res1 = runtime.unsafeRun(s1) tap println

  val s2: Task[Int] = Task.succeed(42)
  val res2 = runtime.unsafeRun(s2) tap println

  //lazy val bigList = (0 to 1000000).toList
  lazy val bigList = (0 to 100).toList
  lazy val bigString = bigList.map(_.toString).mkString(", ")
  val s3 = ZIO.effectTotal(bigString) // ZIO.succeedLazy(bigString) is deprecated
  val res3 = runtime.unsafeRun(s3) tap println

  // ------------------------------------------------------------
  printTextInLine("From Failure Values: .fail")

  val f1 = ZIO.fail("Uh oh!")
  // runtime.unsafeRun(f1)

  val f2 = Task.fail(new Exception("Uh oh!"))
  // runtime.unsafeRun(f2)

  // ------------------------------------------------------------
  printTextInLine(
    "From Scala Values: .fromOption, .fromEither, .fromFunction, .fromFuture")

  // Option
  val zoption: ZIO[Any, Unit, Int] = ZIO.fromOption(Some(2))
  val zoption2: ZIO[Any, String, Int] =
    zoption.mapError(_ => "It wasn't there!")

  // Either
  val zeither: IO[Nothing, String] = ZIO.fromEither(Right("Success!"))

  // Try
  val ztry = ZIO.fromTry(Try(42 / 0))

  // Function1
  val zfun: ZIO[Int, Nothing, Int] = ZIO.fromFunction((i: Int) => i * i)
  //val rzfun = runtime.unsafeRun(zfun)

  // Future

  import scala.concurrent.Future

  lazy val future = Future.successful("Hello!")

  val zfuture: Task[String] =
    ZIO.fromFuture { implicit ec =>
      future.map(_ => "Goodbye!")
    }

  // ------------------------------------------------------------
  printTextInLine("From Synchronous Side-Effects: .effect, .effectTotal")

  val getStrLn: Task[Unit] = ZIO.effect(StdIn.readLine())

  def putStrLn(line: String): UIO[Unit] = ZIO.effectTotal(println(line))

  val getStrLn2: IO[IOException, String] =
    ZIO.effect(StdIn.readLine()).refineToOrDie[IOException]

  // ------------------------------------------------------------
  printTextInLine("From Asynchronous Side-Effects: .effectAsync")

  case class User(name: String)
  case class AuthError(msg: String)

  object legacy {
    def login(onSuccess: User => Unit, onFailure: AuthError => Unit): Unit = ???
  }

  val login: IO[AuthError, User] =
    IO.effectAsync[AuthError, User] { callback =>
      legacy.login(
        user => callback(IO.succeed(user)),
        err => callback(IO.fail(err))
      )
    }

  // ------------------------------------------------------------
  printTextInLine(
    "From Blocking Synchronous Side-Effects: .effectBlocking, .effectBlockingCancelable, .blocking"
  )

  import zio.blocking._

  val sleeping = effectBlocking(Thread.sleep(Long.MaxValue))

  def accept(ss: ServerSocket): ZIO[Blocking, Throwable, Socket] =
    effectBlockingCancelable(ss.accept())(UIO.effectTotal(ss.close()))

  def download(url: String): Task[String] =
    Task.effect {
      Source.fromURL(url)(Codec.UTF8).mkString
    }

  def safeDownload(url: String): ZIO[Blocking, Throwable, String] =
    blocking(download(url))

  printLine()
}
