package overview.ch04handlingresources

import java.io._

import zio.{DefaultRuntime, IO, Task, UIO, ZIO}

import java.nio.file.Files
import java.nio.file.Paths

import scala.util.Try
import scala.util.chaining._
import scala.collection.compat._

import util._
import util.collections._

/*
  type UIO[+A]       = ZIO[Any, Nothing, A]
  type URIO[-R, +A]  = ZIO[R, Nothing, A]
  type Task[+A]      = ZIO[Any, Throwable, A]
  type RIO[-R, +A]   = ZIO[R, Throwable, A]
  type IO[+E, +A]    = ZIO[Any, E, A]
 */
object HandlingResources2 extends App {

  implicit class EitherOps[+L, +R](private val self: Either[L, R]) extends AnyVal {

    @inline def mapLeft[L2](f: L => L2): Either[L2, R] =
      self
        .swap
        .map(f)
        .swap
  }

  // ------------------------------------------------------------
  prtTitleObjectName(this)

  val runtime = new DefaultRuntime {}

  prtSubTitle("Bracket: #bracket")

  def bufferedReader(path: String): Either[IOException, BufferedReader] =
    Try(Files.newBufferedReader(Paths.get(path)))
      .toEither
      .mapLeft {
        case ioe: IOException => ioe
        case t: Throwable     => throw t
      }

  def openFile(path: String): IO[IOException, BufferedReader] =
    IO.fromEither(bufferedReader(path))

  def closeFile(reader: Reader): UIO[Unit] =
    UIO(reader.close())

  def readLines(br: java.io.BufferedReader): Seq[String] =
    Iterator.unfold(())(_ => Option(br.readLine()).map(_ -> ())).toSeq

  def toWords(line: String): List[String] =
    line.split("\\W").toList.filter(_.length > 1)

  val fileName = "build.sbt"

  val counts =
    openFile(fileName)
      .bracket(closeFile) { br =>
        for {
          lines <- ZIO.effect(readLines(br))
          words <- ZIO.succeed(lines flatMap toWords)
        } yield wordCount(words)
      }

  s"word counts in $fileName" tap println
  runtime.unsafeRun(counts) tap println

  /*
  val groupedFileData: IO[IOException, Unit] =
    openFile("data.json").bracket(closeFile(_)) { file =>
      for {
        data    <- decodeData(file)
        grouped <- groupData(data)
      } yield grouped
    }
   */

  prtLine()
}
