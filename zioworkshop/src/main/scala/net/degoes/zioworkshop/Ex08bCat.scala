package net.degoes.zioworkshop

import zio._
import zio.console._
import zio.blocking._
import java.io.IOException
import java.io.BufferedReader
import java.io.Reader

import util.formatting._

object Ex08bCat extends App {

  def openFile(path: String): ZIO[Blocking, IOException, BufferedReader] =
    IO.fromEither(bufferedReader(path))

  def closeFile(reader: Reader): ZIO[Blocking, Nothing, Unit] =
    UIO(reader.close())

  def resource(file: String) =
    ZManaged.make[Blocking, IOException, BufferedReader] {
      openFile(file)
    } {
      closeFile(_)
    }

  /**
    * EXERCISE 8
    *
    * Implement a function to read a file on the blocking thread pool, storing
    * the result into a string.
    */
  def readFile(file: String): ZIO[Blocking, IOException, String] =
    resource(file)
      .use { br =>
        ZIO.fromEither(readLines(br).map(_.mkString("\n")))
      }

  def printContent(file: String): ZIO[ZEnv, IOException, Unit] =
    for {
      content <- readFile(file)
      _       <- putStrLn(content)
      _       <- putStrLn(line(80))
    } yield ()

  /**
    * EXERCISE 9
    *
    * Implement a version of the command-line utility "cat", which dumps the
    * contents of the specified file to standard output.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {

    val effects: List[ZIO[ZEnv, IOException, Unit]] =
      putStrLn(line(80)) :: (args map { file =>
        printContent(file)
      })

    ZIO
      .collectAll(effects)
      .fold(_ => 1, _ => 0)
  }
}
