/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_ref
 */

package datatypes

import zio._
import util.formatting._

object AppRefSema extends util.App {

  // ------------------------------------------------------------
  printTextInLine("Building more sophisticated concurrency primitives: a Semaphore")

  sealed trait Sema {
    def P: UIO[Unit]
    def V: UIO[Unit]
  }

  object Sema {

    def apply(v: Long): UIO[Sema] =
      Ref.make(v).map { vref =>
        new Sema {
          def V = vref.update(_ + 1).unit

          def P =
            (vref.get.flatMap { v =>
              if (v < 0)
                IO.fail(())
              else
                vref
                  .modify(v0 => if (v0 == v) (true, v - 1) else (false, v))
                  .flatMap {
                    case false => IO.fail(())
                    case true  => IO.unit
                  }
            } <> P).either.unit
        }
      }
  }

  import zio.duration.Duration
  import zio.clock._
  import zio.console._
  import zio.random._

  val party = for {
    dancefloor <- Sema(10)
    _ <- ZIO.foreachPar(1 to 100) { dancer =>
          dancefloor.P *> nextDouble
            .map(double => Duration.fromNanos((double * 1000000).round))
            .flatMap { duration =>
              putStrLn(s"Dancer ${dancer}: checking my boots") *>
                sleep(duration) *>
                putStrLn(s"Dancer ${dancer}: dancing like it's 99")
            } *> dancefloor.V
        }
  } yield ()

  new DefaultRuntime {} unsafeRun party
}
