/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_promise
 */

package datatypes

import java.util.concurrent.TimeUnit

import util.formatting._
import util.syntax.pipe._
import zio.duration.Duration
import zio._
import zio.clock.Clock

object AppPromise extends scala.App {

  prtTitleObjectName(this)

  // ------------------------------------------------------------
  prtSubTitle("Promise")

  val runtime = new DefaultRuntime {}

  val race: IO[String, Int] = for {
    p     <- Promise.make[String, Int]
    _     <- p.succeed(1).fork
    _     <- p.complete(ZIO.succeed(2)).fork
    _     <- p.completeWith(ZIO.succeed(3)).fork
    _     <- p.done(Exit.succeed(4)).fork
    _     <- p.fail("5")
    _     <- p.halt(Cause.die(new Error("6")))
    _     <- p.die(new Error("7"))
    _     <- p.interrupt.fork
    value <- p.await
  } yield value

  runtime.unsafeRun(race) |> println

  val ioPromise1: UIO[Promise[Exception, String]] =
    Promise.make[Exception, String]

  val ioBooleanSucceeded: UIO[Boolean] =
    ioPromise1.flatMap(promise => promise.succeed("I'm done"))

  (runtime unsafeRun ioBooleanSucceeded) |> println

  val ioPromise2: UIO[Promise[Exception, Nothing]] =
    Promise.make[Exception, Nothing]

  val ioBooleanFailed: UIO[Boolean] =
    ioPromise2.flatMap(promise => promise.fail(new Exception("boom")))

  (runtime unsafeRun ioBooleanFailed) |> println

  // ------------------------------------------------------------
  prtSubTitle("Awaiting")

  val ioPromise3: UIO[Promise[Exception, String]] =
    Promise.make[Exception, String]

  val ioGet: IO[Exception, String] =
    ioPromise3.flatMap(promise => promise.await)
  //  ioPromise3.complete(IO.succeed("hello"))

  val awaitedValue: ZIO[Any with Clock, Exception, String] = for {
    p      <- ioPromise3
    _      <- p.succeed("hello").delay(Duration(1L, TimeUnit.SECONDS)).fork
    result <- p.await
  } yield result

  (runtime unsafeRun awaitedValue) |> println

  // ------------------------------------------------------------
  prtSubTitle("Polling")

  val ioPromise4: UIO[Promise[Exception, String]] =
    Promise.make[Exception, String]

  val ioIsItDone_orig: UIO[Option[IO[Exception, String]]] = ioPromise4.flatMap(p => p.poll)
  val ioIsItDone2_orig: IO[Unit, IO[Exception, String]]   = ioPromise4.flatMap(p => p.poll.get)

  val ioIsItDone: ZIO[Any with Clock, Unit, IO[Exception, String]] =
    ioPromise4.delay(Duration(1L, TimeUnit.SECONDS)).flatMap(p => p.poll.get)

  val ioIsItDone2: ZIO[Any with Clock, Unit, IO[Exception, String]] =
    ioPromise4.delay(Duration(1L, TimeUnit.SECONDS)).flatMap(p => p.poll.get)

  // ???
  val polledValue: ZIO[Any with Clock, Unit, Serializable] = for {
    p      <- ioPromise4
    _      <- p.succeed("hello").fork
    result <- ioIsItDone race ioIsItDone2
  } yield result

  // (runtime unsafeRun polledValue) |> println

  prtLine()
}
