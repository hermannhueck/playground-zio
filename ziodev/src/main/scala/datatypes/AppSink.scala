/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_sink
 */

package datatypes

import zio._
import zio.stream._

import util.formatting._
import util.syntax.pipe._

object AppSink extends scala.App {

  prtTitleObjectName(this)

  val runtime = new DefaultRuntime {}

  // ------------------------------------------------------------
  prtSubTitle("Sink")

  val stream: Stream[Nothing, Int] = Stream.fromIterable(1 to 100)

  val sink: Sink[Unit, Nothing, Int, Int] = Sink.await[Int]

  val zio: ZIO[Any, Unit, Int] = stream.run(sink)

  (runtime unsafeRun zio) |> println

  // ------------------------------------------------------------
  prtSubTitle("Creating Sinks")

  // def runWithSink[E, A0, A, B](sink: Sink[E, A0, A, B]) =
  //  (runtime unsafeRun stream.run(sink)) |> println

  "----- Sink.await[Int]: Await where sink anticipates for first produced element and returns it:" |> println

  val sink01: Sink[Unit, Nothing, Int, Int] = Sink.await[Int]
  (runtime unsafeRun stream.run(sink01)) |> println

  "----- Sink.collectAll[Int]: Collecting all elements into List[A]:" |> println

  val sink02: Sink[Nothing, Nothing, Int, List[Int]] = Sink.collectAll[Int]
  (runtime unsafeRun stream.run(sink02)) |> println

  "----- Sink.identity[Int].optional: Collecting the first element into an option (returns None for empty streams):" |> println

  val sink03: Sink[Unit, Int, Int, Option[Int]] = Sink.identity[Int].optional
  (runtime unsafeRun stream.run(sink03)) |> println

  "----- Sink.collectAllWhile[Int](condition): Collecting elements until the condition is not satisfied:" |> println

  val sink04: Sink[Nothing, Int, Int, List[Int]] =
    Sink.collectAllWhile[Int](_ > 2)
  (runtime unsafeRun stream.run(sink04)) |> println

  "----- Sink.ignoreWhile[Int](condition): Ignoring incoming values unless some element satisfies the condition:" |> println

  val sink05: Sink[Nothing, Int, Int, Unit] = Sink.ignoreWhile[Int](_ > 2)
  (runtime unsafeRun stream.run(sink05)) |> println

  "----- Sink.drain: Ignoring all the input, used in implementation of stream.runDrain:" |> println

  val sink06: Sink[Nothing, Nothing, Any, Unit] = Sink.drain
  (runtime unsafeRun stream.run(sink06)) |> println

  "----- Sink.fail[E](error): Sink that intentionally fails with given type:" |> println

  import sun.reflect.generics.reflectiveObjects.NotImplementedException

  val sink07: Sink[Exception, Nothing, Any, Nothing] =
    Sink.fail[Exception](new NotImplementedException)
  // runtime unsafeRun stream.run(sink07) //=> throws exception

  "----- Sink.foldLeft: Basic fold accumulation of received elements:" |> println

  val sink08: Sink[Nothing, Nothing, Int, Int] =
    Sink.foldLeft[Int, Int](0)(_ + _)
  (runtime unsafeRun stream.run(sink08)) |> println

  "----- Sink.fromFunction: Mapping over the received input elements:" |> println

  val sink09: ZSink[Any, Unit, Int, Int, List[Int]] =
    Sink.fromFunction[Int, Int](_ * 2).collectAll
  (runtime unsafeRun stream.run(sink09)) |> println

  "----- Sink.pull1: fails with given type in case of empty stream, otherwise continues with provided sink:" |> println

  val sink10: Sink[String, Int, Int, Int] =
    Sink.pull1[String, Int, Int, Int](IO.fail("Empty stream, no value to pull")) { init =>
      Sink.foldLeft[Int, Int](init)(_ + _)
    }
  (runtime unsafeRun stream.run(sink10)) |> println

  "----- Sink.read1 + Sink#collectAll: tries to read head element from stream, fails if isn't present or doesn't satisfy given condition:" |> println

  val sink11: Sink[String, Int, Int, List[Int]] = Sink
    .read1[String, Int] {
      case Some(_) => "Stream is not empty but failed condition"
      case None    => "Stream is empty"
    }(_ > 3)
    .collectAll
  // fails as condition was not met for current stream
  // (runtime unsafeRun stream.run(sink11)) |> println

  // ------------------------------------------------------------
  prtSubTitle("Trasnsforming Sinks")

  "----- Sink#filter: filters the Sink" |> println

  val sink12: Sink[Nothing, Nothing, Int, List[Int]] =
    Sink.collectAll[Int].filter(_ > 90)
  (runtime unsafeRun stream.run(sink12)) |> println

  "----- Sink#race: Running two sinks in parallel and returning the one that completed earlier:" |> println

  val sink13: Sink[Unit, Nothing, Int, Int] = Sink.foldLeft[Int, Int](0)(_ + _) race Sink
    .identity[Int]
  (runtime unsafeRun stream.run(sink13)) |> println

  "----- Sink.contramap: For transforming given input into some sink we can use contramap which is C => A where C is input type and A is sink elements type:" |> println

  val sink14: Sink[Nothing, Nothing, Int, List[String]] =
    Sink.collectAll[String].contramap[Int](_.toString + "id")
  (runtime unsafeRun stream.run(sink14)) |> println

  "----- Sink.dimap: is an extended contramap that additionally transforms sink's output:" |> println

  val sink15: Sink[Nothing, Nothing, Int, List[String]] = Sink
    .collectAll[String]
    .dimap[Int, List[String]](_.toString + "id")(_.take(10))
  (runtime unsafeRun stream.run(sink15)) |> println

  prtLine()
}
