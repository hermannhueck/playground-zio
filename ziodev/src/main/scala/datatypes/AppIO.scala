/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_io
 */

package datatypes

import java.io.{File, IOException}
import java.nio.charset.StandardCharsets

import org.apache.commons.io.FileUtils
import zio._
import util.formatting._
import scala.util.chaining._

object AppIO extends util.App {

  // ------------------------------------------------------------
  printTextInLine("IO")

  val runtime = new DefaultRuntime {}

  // ------------------------------------------------------------
  printTextInLine("Pure Values")

  val uioString: UIO[String] = IO.succeed("Hello World")
  (runtime unsafeRun uioString) pipe println

  // ------------------------------------------------------------
  printTextInLine("Infallible IO")

  // ------------------------------------------------------------
  printTextInLine("Unproductive IO")

  // ------------------------------------------------------------
  printTextInLine("Impure Code: IO.effect, IO.effectTotal, IO#refineOrDie, IO.effectAsync")

  val effectTotalTask: Task[Long] = IO.effectTotal(System.nanoTime())
  (runtime unsafeRun effectTotalTask) pipe println

  println("-----")

  def readFileToByteArray(name: String): IO[IOException, Array[Byte]] =
    IO.effect(FileUtils.readFileToByteArray(new File(name)))
      .refineToOrDie[IOException]

  def readFile(name: String): ZIO[Any, IOException, String] =
    IO.effect(FileUtils.readFileToString(new File(name), StandardCharsets.UTF_8))
      .refineToOrDie[IOException]

  (runtime unsafeRun readFile("README.md")) pipe println

  /*
  def makeRequest(req: Request): IO[HttpException, Response] =
    IO.effectAsync[HttpException, Response](k => Http.req(req, k))
   */

  // ------------------------------------------------------------
  printTextInLine("Mapping: IO#map, IO#mapError")

  val mappedValue: UIO[Int] =
    IO.succeed(21).map(_ * 2)
  (runtime unsafeRun mappedValue) pipe println

  val mappedError: IO[Exception, String] =
    IO.fail("No no!").mapError(msg => new Exception(msg))
  // (runtime unsafeRun mappedError) pipe println // throws Exception

  // ------------------------------------------------------------
  printTextInLine("Chaining: IO#flatMap")

  val chainedActionsValue: UIO[List[Int]] =
    IO.succeed(List(1, 2, 3)).flatMap { list =>
      IO.succeed(list.map(_ + 1))
    }
  (runtime unsafeRun chainedActionsValue) pipe println

  val chainedActionsValueWithForComprehension: UIO[List[Int]] = for {
    list  <- IO.succeed(List(1, 2, 3))
    added <- IO.succeed(list.map(_ + 1))
  } yield added
  (runtime unsafeRun chainedActionsValueWithForComprehension) pipe println

  // ------------------------------------------------------------
  printTextInLine("Brackets")

  "--- see Example: overview.ch04handlingresources.HandlingResources2 ---" pipe println

  "\n--- IO#ensuring" pipe println

  var i: Int                   = 0
  val action: Task[String]     = Task.effectTotal(i += 1) *> Task.fail(new Throwable("Boom!"))
  val cleanupAction: UIO[Unit] = UIO.effectTotal(i -= 1)
  val composite                = action.ensuring(cleanupAction)

  try {
    runtime unsafeRun composite
  } catch {
    case _: Throwable =>
      "\ncaught Exception" pipe println
  } finally {
    s"i = $i" pipe println
  }
}
