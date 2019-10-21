package overview.ch04handlingresources

import zio.{DefaultRuntime, IO, UIO}

import util.formatting._

/*
  type UIO[+A]       = ZIO[Any, Nothing, A]
  type URIO[-R, +A]  = ZIO[R, Nothing, A]
  type Task[+A]      = ZIO[Any, Throwable, A]
  type RIO[-R, +A]   = ZIO[R, Throwable, A]
  type IO[+E, +A]    = ZIO[Any, E, A]
 */
object HandlingResources1 extends App {

  // ------------------------------------------------------------
  prtTitleObjectName(this)

  val runtime = new DefaultRuntime {}

  // ------------------------------------------------------------
  prtSubTitle("Finalizing: #ensuring")

  val finalizer =
    UIO.effectTotal(println("Finalizing!"))

  val finalized: IO[String, Unit] =
    IO.fail("Failed!").ensuring(finalizer)
  val res1 = runtime.unsafeRun(finalized)
  // println(res1)

  prtLine()
}
