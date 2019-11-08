/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_promise
 */

package datatypes

import java.io.IOException

import util.formatting._
import zio._
import zio.clock._
import zio.console._
import zio.duration._

object AppPromiseExample extends scala.App {

  printHeaderWithProgramName(this)

  // ------------------------------------------------------------
  printTextInLine("Promise Example Usage")

  val runtime = new DefaultRuntime {}

  val program: ZIO[Console with Clock, IOException, Unit] =
    for {
      promise <- Promise.make[Nothing, String]
      sendHelloWorld = (IO.succeed("hello world") <*
        sleep(1.second))
        .flatMap(promise.succeed)
      getAndPrint = promise.await.flatMap(putStrLn)
      fiberA <- sendHelloWorld.fork
      fiberB <- getAndPrint.fork
      _ <- (fiberA zip fiberB).join
    } yield ()

  runtime unsafeRun program

  printLine()
}
