package overview.ch02basicoperations

import zio.{DefaultRuntime, IO, Task, UIO, ZIO}

import scala.util.chaining._
import scala.io.StdIn

import util._

/*
  type UIO[+A]       = ZIO[Any, Nothing, A]
  type URIO[-R, +A]  = ZIO[R, Nothing, A]
  type Task[+A]      = ZIO[Any, Throwable, A]
  type RIO[-R, +A]   = ZIO[R, Throwable, A]
  type IO[+E, +A]    = ZIO[Any, E, A]
 */
object BasicOperations extends App {

  // ------------------------------------------------------------
  prtTitleObjectName(this)

  val runtime = new DefaultRuntime {}

  // ------------------------------------------------------------
  prtSubTitle("Mapping: #map, #mapError")

  val succeded: UIO[Int] = IO.succeed(21).map(_ * 2)
  val res1               = runtime.unsafeRun(succeded) tap println

  val failed: IO[Exception, Unit] =
    IO.fail("No no!").mapError(msg => new Exception(msg))
  // val res2 = runtime.unsafeRun(failed) tap println // throws java.lang.Exception: No no!

  // ------------------------------------------------------------
  prtSubTitle("Chaining: #flatMap")

  val getStrLn: Task[String]            = ZIO.effect(StdIn.readLine())
  def putStrLn(line: String): UIO[Unit] = ZIO.effectTotal(println(line))

  val sequenced =
    getStrLn.flatMap(input => putStrLn(s"You entered: $input"))
  println("Enter a string:")
  val res3 = runtime.unsafeRun(sequenced) tap println

  // ------------------------------------------------------------
  prtSubTitle("For Comprehensions")

  val program =
    for {
      _    <- putStrLn("Hello! What is your name?")
      name <- getStrLn
      _    <- putStrLn(s"Hello, ${name}, welcome to ZIO!")
    } yield ()
  val res4 = runtime.unsafeRun(program) tap println

  // ------------------------------------------------------------
  prtSubTitle("Zipping: #zip, #zipLeft, #zipRight, <*, *>")

  val zipped: UIO[(String, Int)] =
    ZIO.succeed("4").zip(ZIO.succeed(2))
  val res5 = runtime.unsafeRun(zipped) tap println

  val zipRight1 =
    putStrLn("What is your name?").zipRight(getStrLn)
  val res6 = runtime.unsafeRun(zipRight1) tap println

  val zipRight2 =
    putStrLn("What is your name?") *> getStrLn
  val res7 = runtime.unsafeRun(zipRight2) tap println

  prtLine()
}
