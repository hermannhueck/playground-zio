/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_ref
 */

package datatypes

import zio._
import util.formatting._
import util.syntax.pipe._

object AppRef extends scala.App {

  prtTitleObjectName(this)

  // ------------------------------------------------------------
  prtSubTitle("Ref: Ref.make, Ref#get, Ref#set")

  val runtime = new DefaultRuntime {}

  val zio1 = for {
    ref <- Ref.make(100)
    v1  <- ref.get
    v2  <- ref.set(v1 - 50)
    v3  <- ref.get
  } yield v3

  runtime.unsafeRun(zio1) |> println

  // ------------------------------------------------------------
  prtSubTitle("Updating a Ref: Ref#update")

  def repeat[E, A](n: Int)(io: IO[E, A]): IO[E, Unit] =
    Ref.make(0).flatMap { iRef =>
      def loop: IO[E, Unit] = iRef.get.flatMap { i =>
        if (i < n)
          io *> iRef.update(_ + 1) *> loop
        else
          IO.unit
      }
      loop
    }

  val io = repeat(3)(IO.effect(println("Hello world")))
  runtime unsafeRun io

  // ------------------------------------------------------------
  prtSubTitle("State Transformers: Ref#modify")

  "--- classic state mutation without ZIO ---" |> println
  var idCounter = 0

  def freshVar: String = {
    idCounter += 1
    s"idCounter = ${idCounter}"
  }

  val v1 = freshVar
  val v2 = freshVar
  val v3 = freshVar
  v3 |> println

  "--- functional mutation with ZIO's Ref ---" |> println

  val uioFinalId = Ref.make(0).flatMap { idCounter =>
    def freshVar: UIO[String] =
      idCounter.modify(cpt => (s"idCounter = ${cpt + 1}", cpt + 1))

    for {
      v1 <- freshVar
      v2 <- freshVar
      v3 <- freshVar
    } yield v3
  }

  (runtime unsafeRun uioFinalId) |> println

  prtLine()
}