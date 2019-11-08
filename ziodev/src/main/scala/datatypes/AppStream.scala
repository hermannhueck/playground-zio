/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_stream
 */

package datatypes

import zio._
import zio.stream._
import zio.console._

import cats.implicits._

import util.formatting._
import scala.util.chaining._

object AppStream extends scala.App {

  printHeaderWithProgramName(this)

  val runtime = new DefaultRuntime {}

  // ------------------------------------------------------------
  printTextInLine("Stream")

  // ------------------------------------------------------------
  printTextInLine("Creating a Stream")

  val stream: Stream[Nothing, Int] = Stream(1, 2, 3)

  val streamFromIterable: Stream[Nothing, Int] = Stream.fromIterable(0 to 100)

  // ------------------------------------------------------------
  printTextInLine("Transforming a Stream: #map")

  val intStream: Stream[Nothing, Int] = Stream.fromIterable(0 to 100)
  val stringStream: Stream[Nothing, String] = intStream.map(_.toString)

  // ------------------------------------------------------------
  printTextInLine("Consuming a Stream: #foreach")

  val result: RIO[Console, Unit] =
    Stream
      .fromIterable(0 to 10)
      .foreach(i => putStrLn(i.toString))

  runtime unsafeRun result

  // ------------------------------------------------------------
  printTextInLine("Using a Sink: #run")

  def streamReduce(total: Int, element: Int): Int = total + element

  val resultFromSink: UIO[Int] =
    Stream(1, 2, 3).run(Sink.foldLeft(0)(streamReduce))

  runtime unsafeRun resultFromSink

  // ------------------------------------------------------------
  printTextInLine("Working on several streams")

  "--- Stream#merge:" pipe println

  val merged: Stream[Nothing, Int] = Stream(1, 2, 3).merge(Stream(11, 12, 13))
  val resMerged: ZIO[Any, Nothing, List[Int]] = merged.run(Sink.collectAll[Int])
  (runtime unsafeRun resMerged) pipe println

  "--- Stream#zip:" pipe println

  val zipped: Stream[Nothing, (Int, Int)] =
    Stream(1, 2, 3).zip(Stream(11, 12, 13))
  val resZipped: ZIO[Any, Nothing, List[(Int, Int)]] =
    zipped.run(Sink.collectAll[(Int, Int)])

  (runtime unsafeRun resZipped) pipe println

  def tupleStreamReduce(total: Int, element: (Int, Int)) = {
    val (a, b) = element
    total + (a + b)
  }

  val reducedResult: UIO[Int] = zipped.run(Sink.foldLeft(0)(tupleStreamReduce))

  (runtime unsafeRun reducedResult) pipe (reduced =>
    println(s"tuples reduced: $reduced"))

  "--- Stream#zipWith:" pipe println

  val zippedWith: Stream[Nothing, Int] =
    Stream(1, 2, 3)
      .zipWith(Stream(11, 12, 13)) {
        case (opt1, opt2) => opt1 -> opt2 mapN (_ + _)
      }
  val resZippedWith: ZIO[Any, Nothing, List[Int]] =
    zippedWith.run(Sink.collectAll[Int])
  (runtime unsafeRun resZippedWith) pipe println

  "--- Stream#zipWithIndex:" pipe println

  val zippedWithIndex: Stream[Nothing, String] =
    Stream(10, 11, 12).zipWithIndex
      .map(_.swap)
      .map { case (index, value) => s"index = $index, value = $value" }

  val resZippedWithIndex: ZIO[Any, Nothing, List[String]] =
    zippedWithIndex.run(Sink.collectAll[String])

  runtime unsafeRun resZippedWithIndex foreach println

  printLine()
}
