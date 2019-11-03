/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_semaphore
 */

package datatypes

import java.util.concurrent.TimeUnit

import zio._
import zio.console._
import zio.duration.Duration

import util.formatting._
import scala.util.chaining._

object AppSemaphore extends scala.App {

  prtTitleObjectName(this)

  val runtime = new DefaultRuntime {}

  // ------------------------------------------------------------
  prtSubTitle("Semaphore: Semaphore.make, Semaphore#acquire, Semaphore#release")

  val task = for {
    _ <- putStrLn("start")
    _ <- putStrLn("sleeping 2 seconds ...")
    _ <- ZIO.sleep(Duration(2, TimeUnit.SECONDS))
    _ <- putStrLn("end\n")
  } yield ()

  val semTask = (sem: Semaphore) =>
    for {
      _ <- sem.acquire
      _ <- task
      _ <- sem.release
    } yield ()

  val semTaskSeq = (sem: Semaphore) => (1 to 3).map(_ => semTask(sem))

  val program = for {
    sem <- Semaphore.make(permits = 1)
    seq <- ZIO.effectTotal(semTaskSeq(sem))
    _   <- ZIO.collectAllPar(seq)
  } yield ()

  runtime unsafeRun program

  "--- Semaphore#acquireN, Semaphore#releaseN ---" pipe println

  val semTaskN = (sem: Semaphore) =>
    for {
      _ <- sem.acquireN(5)
      _ <- task
      _ <- sem.releaseN(5)
    } yield ()

  "--- Semaphore#withPermit, Semaphore#withPermits ---" pipe println

  val permitTask = (sem: Semaphore) =>
    for {
      _ <- sem.withPermit(task)
    } yield ()

  prtLine()
}
