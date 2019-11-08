package datatypes

import java.io.{BufferedReader, IOException}

import zio._
import util.formatting._
import scala.util.chaining._

object AppManaged extends util.App {

  // ------------------------------------------------------------
  printTextInLine("Managed: Managed.make")

  val runtime = new DefaultRuntime {}

  def doSomething(queue: Queue[Int]): Task[Unit] =
    IO.effect(println(s"... using the Queue $queue"))

  val managedResource = Managed.make(Queue.unbounded[Int])(_.shutdown)

  val usedResource: Task[Unit] = managedResource.use { queue =>
    doSomething(queue)
  }

  runtime unsafeRun usedResource

  // ------------------------------------------------------------
  printTextInLine("Creating a Managed: Managed.fromEffect")

  "--- from Effect ---" pipe println

  def acquire: IO[String, Int] =
    IO.effect {
        println("Acquiring an Int resource ...")
        42
      }
      .refineToOrDie[String]

  val managedFromEffect: Managed[String, Int] = Managed.fromEffect(acquire)

  runtime unsafeRun managedFromEffect.use(_ => IO.effect(println("Using the Int resource ...")))

  "--- from pure value ---" pipe println

  val managedFromValue: Managed[Nothing, Int] = Managed.succeed(3)

  runtime unsafeRun managedFromValue.use { value =>
    IO.effect(println(s"Using the value $value ..."))
  }

  // ------------------------------------------------------------
  printTextInLine("Managed with ZIO environment: ZManaged")

  import zio.console._

  val zManagedResource: ZManaged[Console, Nothing, Unit] =
    ZManaged.make(console.putStrLn("acquiring ..."))(_ => console.putStrLn("releasing ..."))

  val zUsedResource: ZIO[Console, Nothing, Unit] = zManagedResource.use { _ =>
    console.putStrLn("running ...")
  }

  runtime unsafeRun zUsedResource

  // ------------------------------------------------------------
  printTextInLine("Combining Managed: Managed#flatMap")

  import overview.ch04handlingresources.HandlingResources2.{closeFile, openFile}

  val managedQueue: Managed[Nothing, Queue[Int]] =
    Managed.make(Queue.unbounded[Int])(_.shutdown)

  val managedFile: Managed[IOException, BufferedReader] =
    Managed.make(openFile("README.md"))(closeFile)

  val combined: Managed[IOException, (Queue[Int], BufferedReader)] = for {
    queue  <- managedQueue
    reader <- managedFile
  } yield (queue, reader)

  val usedCombinedResource: IO[IOException, Unit] = combined.use {
    case (queue, reader) => doSomething(queue, reader)
  }

  def doSomething(queue: Queue[Int], reader: BufferedReader): IO[IOException, Unit] =
    IO.effect(println(s"... using the Queue: $queue and BufferedReader: $reader ..."))
      .refineToOrDie[IOException]

  runtime unsafeRun usedCombinedResource
}
