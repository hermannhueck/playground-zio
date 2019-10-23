/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_io
 */

package datatypes

import java.io.{File, FileInputStream}
import java.nio.charset.StandardCharsets

import util.formatting._
import zio.{App, Task, UIO}

object AppIOUsingBracket extends App {

  // run my bracket
  def run(args: List[String]): UIO[Int] =
    mybracket.orDie.as(0)

  def closeStream(is: FileInputStream): UIO[Unit] =
    UIO(is.close())

  // helper method to work around in Java 8
  def readAll(fis: FileInputStream, len: Long): Array[Byte] = {
    val content: Array[Byte] = Array.ofDim(len.toInt)
    fis.read(content)
    content
  }

  def convertBytes(is: FileInputStream, len: Long): Task[String] =
    // Task.effect(println(new String(is.readAllBytes(), StandardCharsets.UTF_8))) // Java 11+
    Task.effect(new String(readAll(is, len), StandardCharsets.UTF_8)) // Java 8

  // mybracket is just a value. Won't execute anything here until interpreted
  val mybracket: Task[Unit] = for {
    _    <- Task.effect(prtTitleObjectName(this))
    file <- Task(new File("README.md"))
    len  = file.length
    string <- Task(new FileInputStream(file))
               .bracket(closeStream)(convertBytes(_, len))
    _ <- Task.effect(println(string))
    _ <- Task.effect(prtLine())
  } yield ()
}
