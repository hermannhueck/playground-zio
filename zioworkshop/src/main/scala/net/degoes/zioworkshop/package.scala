package net.degoes

package object zioworkshop {

  import java.io.IOException
  import java.io.InputStream
  import java.io.BufferedReader
  import java.nio.file.Paths
  import java.nio.file.Files

  import scala.util.Try

  import util.formatting._

  import _root_.zio._

  val managed =
    Managed.make {
      IO.succeed(prtLine())
    } { _ =>
      IO.succeed(prtLine())
    }

  def withLines[A](thunk: => ZIO[ZEnv, Nothing, A]): ZIO[ZEnv, Nothing, A] =
    managed.use(_ => thunk)

  implicit class PipeOperator[A](private val a: A) extends AnyVal {
    @inline def pipe[B](f: A => B): B = f(a)
  }

  import util.syntax.either.EitherOps

  def ioOperation[RESULT](ioOp: => RESULT): Either[IOException, RESULT] =
    Try(ioOp)
      .toEither
      .mapLeft {
        case ioe: IOException => ioe
        case t: Throwable     => throw t
      }

  def inputStream(path: String): Either[IOException, InputStream] =
    ioOperation {
      Files.newInputStream(Paths.get(path))
    }

  def bufferedReader(path: String): Either[IOException, BufferedReader] =
    ioOperation {
      Files.newBufferedReader(Paths.get(path))
    }

  import compat213.collections.unfold._

  def readLines(br: java.io.BufferedReader): Either[IOException, Seq[String]] =
    ioOperation {
      Iterator.unfold(())(_ => Option(br.readLine()).map(x => (x, ()))).toSeq
    }
}
