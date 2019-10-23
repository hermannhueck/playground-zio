package datatypes

import java.io.{BufferedReader, IOException}

import zio._
import util.formatting._
import util.syntax.pipe._

object AppManaged extends scala.App {

  prtTitleObjectName(this)

  // ------------------------------------------------------------
  prtSubTitle("Managed: Managed.make")

  val runtime = new DefaultRuntime {}

  def doSomething(queue: Queue[Int]): Task[Unit] =
    IO.effect(println("... using the Queue ..."))

  val managedResource = Managed.make(Queue.unbounded[Int])(_.shutdown)

  val usedResource: Task[Unit] = managedResource.use { queue =>
    doSomething(queue)
  }

  runtime unsafeRun usedResource

  // ------------------------------------------------------------
  prtSubTitle("Creating a Managed: Managed.fromEffect")

  "--- from Effect ---" |> println

  def acquire: IO[String, Int] =
    IO.effect {
        println("Acquiring an Int resource ...")
        42
      }
      .refineToOrDie[String]

  val managedFromEffect: Managed[String, Int] = Managed.fromEffect(acquire)

  runtime unsafeRun managedFromEffect.use(_ => IO.effect(println("Using the Int resource ...")))

  "--- from pure value ---" |> println

  val managedFromValue: Managed[Nothing, Int] = Managed.succeed(3)

  runtime unsafeRun managedFromValue.use { value =>
    IO.effect(println(s"Using the value $value ..."))
  }

  // ------------------------------------------------------------
  prtSubTitle("Managed with ZIO environment: ZManaged")

  import zio.console._

  val zManagedResource: ZManaged[Console, Nothing, Unit] =
    ZManaged.make(console.putStrLn("acquiring ..."))(_ => console.putStrLn("releasing ..."))

  val zUsedResource: ZIO[Console, Nothing, Unit] = zManagedResource.use { _ =>
    console.putStrLn("running ...")
  }

  runtime unsafeRun zUsedResource

  // ------------------------------------------------------------
  prtSubTitle("Combining Managed: Managed#flatMap")

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
    IO.effect(println("... using the Queue and BufferedReader ..."))
      .refineToOrDie[IOException]

  runtime unsafeRun usedCombinedResource

  prtLine()
}
