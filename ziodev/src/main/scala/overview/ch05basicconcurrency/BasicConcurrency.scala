package overview.ch05basicconcurrency

import scala.util.chaining._

import zio.clock.Clock
import zio.{DefaultRuntime, Exit, Fiber, IO, UIO, ZIO}

import util.formatting._

/*
  type UIO[+A]       = ZIO[Any, Nothing, A]
  type URIO[-R, +A]  = ZIO[R, Nothing, A]
  type Task[+A]      = ZIO[Any, Throwable, A]
  type RIO[-R, +A]   = ZIO[R, Throwable, A]
  type IO[+E, +A]    = ZIO[Any, E, A]
 */
object BasicConcurrency extends App {

  // ------------------------------------------------------------
  prtTitleObjectName(this)

  val runtime = new DefaultRuntime {}

  // ------------------------------------------------------------
  prtSubTitle("Forking Effects: Fiber#fork")

  def fib(n: Long): UIO[Long] =
    UIO {
      if (n <= 1) UIO.succeed(n)
      else fib(n - 1).zipWith(fib(n - 2))(_ + _)
    }.flatten

  val fib100Fiber: UIO[Fiber[Nothing, Long]] =
    for {
      fiber <- fib(35).fork
    } yield fiber

  val fiber: Fiber[Nothing, Long] = runtime.unsafeRun(fib100Fiber)
  val res1: IO[Nothing, Long]     = fiber.join
  runtime.unsafeRun(res1) tap println

  // ------------------------------------------------------------
  prtSubTitle("Joining Fibers: Fiber#join")

  val zio2: ZIO[Any, Nothing, String] = for {
    fiber   <- IO.succeed("Hi!").fork
    message <- fiber.join
  } yield message
  val res2: String = runtime.unsafeRun(zio2) tap println

  // ------------------------------------------------------------
  prtSubTitle("Awaiting Fibers: Fiber#await")

  val zio3: ZIO[Any, Nothing, Exit[Nothing, String]] = for {
    fiber <- IO.succeed("Hi!").fork
    exit  <- fiber.await
  } yield exit
  val res3 = runtime.unsafeRun(zio3) tap println

  // ------------------------------------------------------------
  prtSubTitle("Interrupting Fibers: Fiber#interrupt")

  val zio4: ZIO[Any, Nothing, Exit[Nothing, String]] = for {
    fiber <- IO.succeed("Hi!").forever.fork
    exit  <- fiber.interrupt
  } yield exit
  val res4: Exit[Nothing, String] = runtime.unsafeRun(zio4) // tap println

  val zio5: ZIO[Any, Nothing, Unit] = for {
    fiber <- IO.succeed("Hi!").forever.fork
    _     <- fiber.interrupt.fork
  } yield ()
  val res5: Unit = runtime.unsafeRun(zio5) // tap println

  // ------------------------------------------------------------
  prtSubTitle("Composing Fibers: zip, zipWith, orElse")

  val zio6: ZIO[Any, Nothing, (String, String)] = for {
    fiber1 <- IO.succeed("Hi!").fork
    fiber2 <- IO.succeed("Bye!").fork
    fiber  = fiber1.zip(fiber2)
    tuple  <- fiber.join
  } yield tuple
  val res6: (String, String) = runtime.unsafeRun(zio6) tap println

  val zio7: ZIO[Any, String, String] = for {
    fiber1 <- IO.fail("Uh oh!").fork
    fiber2 <- IO.succeed("Hurray!").fork
    fiber  = fiber1.orElse(fiber2)
    tuple  <- fiber.join
  } yield tuple
  val res7: String = runtime.unsafeRun(zio7) tap println

  // ------------------------------------------------------------
  prtSubTitle("Parallelism")
  "sequential operations: ZIO#zip,    zipWith,    collectAll,    foreach,    reduceAll,    mergeAll" tap println
  "parallel operations  : ZIO#zipPar, zipWithPar, collectAllPar, foreachPar, reduceAllPar, mergeAllPar" tap println

  // ------------------------------------------------------------
  prtSubTitle("Racing: Fiber#race")

  val zio8: ZIO[Any, Nothing, String] = for {
    winner <- IO.succeed("Hello") race IO.succeed("Goodbye")
  } yield winner
  val res8: String = runtime.unsafeRun(zio8) tap println

  val zio9: ZIO[Any, Nothing, Either[String, Int]] = for {
    winner <- IO.succeed(5 * 5).either race IO.succeed(8 * 8).either
  } yield winner
  val res9: Either[String, Int] = runtime.unsafeRun(zio9) tap println

  // ------------------------------------------------------------
  prtSubTitle("Timeout")

  import zio.duration._

  val zio10: ZIO[Any with Clock, Nothing, Option[String]] = IO.succeed("Hello").timeout(10.seconds)
  val res10: Option[String]                               = runtime.unsafeRun(zio10) tap println

  prtLine()
}
