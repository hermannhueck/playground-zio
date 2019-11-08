package overview.ch03handlingerrors

import java.io.{FileNotFoundException, IOException}

import zio.{DefaultRuntime, IO, Schedule, UIO, ZIO}

import scala.util.chaining._
import java.nio.file.Files
import java.nio.file.Paths
import scala.util.Try

import util.formatting._

/*
  type UIO[+A]       = ZIO[Any, Nothing, A]
  type URIO[-R, +A]  = ZIO[R, Nothing, A]
  type Task[+A]      = ZIO[Any, Throwable, A]
  type RIO[-R, +A]   = ZIO[R, Throwable, A]
  type IO[+E, +A]    = ZIO[Any, E, A]
 */
object HandlingErrors extends App {

  // ------------------------------------------------------------
  printHeaderWithProgramName(this)

  val runtime = new DefaultRuntime {}

  // ------------------------------------------------------------
  printTextInLine("Either: #either, #absolve")

  // You can surface failures with ZIO#either, which takes an ZIO[R, E, A] and produces an ZIO[R, Nothing, Either[E, A]].

  val failed: IO[String, Int] = IO.fail("Uh oh!")
  val zeither: UIO[Either[String, Int]] = failed.either
  val res1: Either[String, Int] = runtime.unsafeRun(zeither) tap println

  def sqrt(io: UIO[Double]): IO[String, Double] =
    ZIO.absolve(
      io.map { value =>
        if (value < 0.0)
          Left("Value must be >= 0.0")
        else
          Right(Math.sqrt(value))
      }
    )
  val res2: Double = runtime.unsafeRun(sqrt(UIO.succeed(3.0))) tap println

  // ------------------------------------------------------------
  printTextInLine("Catching All Errors: #catchAll")

  def openFile(path: String): IO[IOException, Array[Byte]] =
    IO.fromEither {
        Try(Files.readAllBytes(Paths.get(path))).toEither
      }
      .mapError {
        case ioe: IOException => ioe
        case t: Throwable     => throw t
      }

  val z: IO[IOException, Array[Byte]] =
    openFile("primary.json")
      .catchAll(_ => openFile("backup.json"))

  // ------------------------------------------------------------
  printTextInLine("Catching Some Errors: #catchSome")

  val data: IO[IOException, Array[Byte]] =
    openFile("primary.data")
      .catchSome {
        case _: FileNotFoundException =>
          openFile("backup.data")
      }

  // ------------------------------------------------------------
  printTextInLine("Fallback: #orElse")

  val primaryOrBackupData: IO[IOException, Array[Byte]] =
    openFile("primary.data")
      .orElse(openFile("backup.data"))

  // ------------------------------------------------------------
  printTextInLine("Folding: #fold")

  lazy val DefaultData: Array[Byte] = Array(0, 0)

  val primaryOrDefaultData: UIO[Array[Byte]] =
    openFile("primary.data").fold(_ => DefaultData, data => data)

  val primaryOrSecondaryData: IO[IOException, Array[Byte]] =
    openFile("primary.data")
      .foldM(_ => openFile("secondary.data"), data => ZIO.succeed(data))

  /*
  trait Content
  final case class NoContent(error: String) extends Content
  final case class Content(a: Array[Byte]) extends Content

  def readUrls(config: String): ZIO[Any, String, Array[Byte]] = ???
  def fetchContent(bytes: Array[Byte]): Content = ???

  val urls: UIO[Content] =
    readUrls("urls.json").foldM(
      error   => IO.succeedLazy(NoContent(error)),
      success => fetchContent(success)
    )
   */

  // ------------------------------------------------------------
  printTextInLine("Retrying: #retry, #retryOrElse, #retryOrElseEither")

  import zio.clock._

  val retriedOpenFile: ZIO[Clock, IOException, Array[Byte]] =
    openFile("primary.data").retry(Schedule.recurs(5))

  /*
  openFile("primary.data").retryOrElse(
    Schedule.recurs(5),
    (_, _) => ZIO.succeed(DefaultData))
   */

  printLine()
}
