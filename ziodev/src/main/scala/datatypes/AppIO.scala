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
import util.syntax.pipe._

object AppIO extends scala.App {

  prtTitleObjectName(this)

  // ------------------------------------------------------------
  prtSubTitle("IO")

  val runtime = new DefaultRuntime {}

  // ------------------------------------------------------------
  prtSubTitle("Pure Values")

  val uioString: UIO[String] = IO.succeed("Hello World")
  (runtime unsafeRun uioString) |> println

  // ------------------------------------------------------------
  prtSubTitle("Infallible IO")

  // ------------------------------------------------------------
  prtSubTitle("Unproductive IO")

  // ------------------------------------------------------------
  prtSubTitle("Impure Code: IO.effect, IO.effectTotal, IO#refineOrDie, IO.effectAsync")

  val effectTotalTask: Task[Long] = IO.effectTotal(System.nanoTime())
  (runtime unsafeRun effectTotalTask) |> println

  println("-----")

  def readFileToByteArray(name: String): IO[IOException, Array[Byte]] =
    IO.effect(FileUtils.readFileToByteArray(new File(name)))
      .refineToOrDie[IOException]

  def readFile(name: String): ZIO[Any, IOException, String] =
    IO.effect(FileUtils.readFileToString(new File(name), StandardCharsets.UTF_8))
      .refineToOrDie[IOException]

  (runtime unsafeRun readFile("README.md")) |> println

  /*
  def makeRequest(req: Request): IO[HttpException, Response] =
    IO.effectAsync[HttpException, Response](k => Http.req(req, k))
   */

  // ------------------------------------------------------------
  prtSubTitle("Mapping: IO#map, IO#mapError")

  val mappedValue: UIO[Int] =
    IO.succeed(21).map(_ * 2)
  (runtime unsafeRun mappedValue) |> println

  val mappedError: IO[Exception, String] =
    IO.fail("No no!").mapError(msg => new Exception(msg))
  // (runtime unsafeRun mappedError) |> println // throws Exception

  // ------------------------------------------------------------
  prtSubTitle("Chaining: IO#flatMap")

  val chainedActionsValue: UIO[List[Int]] =
    IO.succeed(List(1, 2, 3)).flatMap { list =>
      IO.succeed(list.map(_ + 1))
    }
  (runtime unsafeRun chainedActionsValue) |> println

  val chainedActionsValueWithForComprehension: UIO[List[Int]] = for {
    list  <- IO.succeed(List(1, 2, 3))
    added <- IO.succeed(list.map(_ + 1))
  } yield added
  (runtime unsafeRun chainedActionsValueWithForComprehension) |> println

  // ------------------------------------------------------------
  prtSubTitle("Brackets")

  "--- see Example: overview.ch04handlingresources.HandlingResources2 ---" |> println

  "\n--- IO#ensuring" |> println

  var i: Int                   = 0
  val action: Task[String]     = Task.effectTotal(i += 1) *> Task.fail(new Throwable("Boom!"))
  val cleanupAction: UIO[Unit] = UIO.effectTotal(i -= 1)
  val composite                = action.ensuring(cleanupAction)

  try {
    runtime unsafeRun composite
  } catch {
    case t: Throwable =>
      "\ncaught Exception" |> println
  } finally {
    s"i = $i" |> println
  }

  prtLine()
}
