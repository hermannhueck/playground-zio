package net.degoes.zioworkshop

import zio._
import zio.console._
import zio.blocking._
import java.io._
import util.formatting._

object Ex10CatIncremental extends App {

  /**
    * EXERCISE 10
    *
    * Implement all missing methods of `FileHandle`. Be sure to do all work on
    * the blocking thread pool.
    */
  final case class FileHandle private (private val is: InputStream) {

    def close: ZIO[Blocking, Nothing, Unit] =
      UIO(is.close())

    def read: ZIO[Blocking, IOException, Option[Chunk[Byte]]] =
      ZIO.fromEither(ioOperation {
        doRead()
      })

    val MaxRead = 1024 * 16

    def doRead(): Option[Chunk[Byte]] =
      readChunck(is, new Array[Byte](MaxRead), Chunk.empty)
        .pipe { chunk =>
          if (chunk.isEmpty)
            Option.empty[Chunk[Byte]]
          else
            Some(chunk)
        }

    @scala.annotation.tailrec
    private def readChunck(
        is: InputStream,
        array: Array[Byte],
        acc: Chunk[Byte]
    ): Chunk[Byte] = {
      // println(s">>>>>>>>>>> Reading (max = ${array.length}) ...")
      val nRead = is.read(array)
      val newChunk = Chunk.fromArray(array)
      if (nRead < array.length)
        acc ++ newChunk.take(nRead)
      else
        readChunck(is, new Array(array.length), acc ++ newChunk)
    }
  }

  object FileHandle {

    final def open(file: String): ZIO[Blocking, IOException, FileHandle] =
      IO.fromEither(inputStream(file)) map FileHandle.apply
  }

  def decode(chunk: Chunk[Byte]): String =
    chunk.map(_.toChar).mkString

  def contentOf(file: String): ZIO[Blocking, IOException, Option[String]] =
    contentOf2(file)
      .map(_.map(decode)) // decode Chunk to String

  def contentOf1(
      file: String): ZIO[Blocking, IOException, Option[Chunk[Byte]]] =
    FileHandle
      .open(file)
      .bracket(_.close)(_.read)

  def contentOf2(
      file: String): ZIO[Blocking, IOException, Option[Chunk[Byte]]] =
    ZManaged
      .make(FileHandle.open(file))(_.close)
      .use(_.read)

  def printContent(file: String): ZIO[ZEnv, IOException, Unit] =
    for {
      _ <- putStrLn(textInLine(file))
      content <- contentOf(file)
      _ <- putStr(content.getOrElse("<empty file>"))
    } yield ()

  /**
    * EXERCISE 11
    *
    * Implement an incremental version of the `cat` utility, using `ZIO#bracket`
    * or `ZManaged` to ensure the file is closed in the event of error or
    * interruption.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {

    val effects: List[ZIO[ZEnv, IOException, Unit]] =
      (putStrLn(header(objectName(this))) :: (args map { file =>
        printContent(file)
      })) :+ putStrLn(line())

    ZIO
      .collectAll(effects)
      .fold(_ => 1, _ => 0)
  }
}
