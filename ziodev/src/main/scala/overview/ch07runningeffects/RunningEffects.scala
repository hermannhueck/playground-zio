package overview.ch07runningeffects

import zio.{DefaultRuntime, Runtime, UIO, ZIO}

import util.formatting._

/*
  type UIO[+A]       = ZIO[Any, Nothing, A]
  type URIO[-R, +A]  = ZIO[R, Nothing, A]
  type Task[+A]      = ZIO[Any, Throwable, A]
  type RIO[-R, +A]   = ZIO[R, Throwable, A]
  type IO[+E, +A]    = ZIO[Any, E, A]
 */
object RunningEffects extends App {

  // ------------------------------------------------------------
  printHeaderWithProgramName(this)

  def putStrLn(line: String): UIO[Unit] = ZIO.effectTotal(println(line))

  // ------------------------------------------------------------
  printTextInLine("DefaultRuntime")

  val runtime = new DefaultRuntime {}
  runtime.unsafeRun(putStrLn("Hello World!"))

  // ------------------------------------------------------------
  printTextInLine("Custom Runtime")

  import zio.internal.PlatformLive

  val myRuntime: Runtime[Int] = Runtime(42, PlatformLive.Default)
  myRuntime.unsafeRun(putStrLn("Hello World!"))

  printLine()
}
