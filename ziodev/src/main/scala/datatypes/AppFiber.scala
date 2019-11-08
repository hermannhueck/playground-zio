/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_fiber
 */

package datatypes

import zio._
import util.formatting._
import scala.util.chaining._

object AppFiber extends scala.App {

  printHeaderWithProgramName(this)

  // ------------------------------------------------------------
  printTextInLine("Fiber: Fiber#fork, Fiber#join, Fiber#interrupt")

  val runtime = new DefaultRuntime {}

  "--- computing Analysis ---" pipe println

  case class Analysis(data: List[Int]) {
    override def toString: String = "Analysis(data ...)"
  }

  case class Validation(data: List[Int]) {
    def validate(): Boolean = true
    override def toString: String = "AnalValidationysis(data ...)"
  }

  def analyzeData(data: List[Int]): UIO[Analysis] =
    UIO.succeed(Analysis(data))

  def validateData(data: List[Int]): UIO[Boolean] =
    IO.succeed(Validation(data).validate())

  val data = List(1, 2, 3)

  val analyzed: ZIO[Any, Nothing, Analysis] =
    for {
      fiber1 <- analyzeData(data).fork // IO[E, Analysis]
      fiber2 <- validateData(data).fork // IO[E, Boolean]
      // Do other stuff
      valid <- fiber2.join
      _ <- if (!valid) fiber1.interrupt
      else IO.unit
      analyzed <- fiber1.join
    } yield analyzed

  val analysis: Analysis = runtime unsafeRun analyzed
  analysis pipe println

  "--- Fibonacci ---" pipe println

  def fib(n: Int): UIO[Int] =
    if (n <= 1) {
      IO.succeed(1)
    } else {
      for {
        fiber1 <- fib(n - 2).fork
        fiber2 <- fib(n - 1).fork
        v2 <- fiber2.join
        v1 <- fiber1.join
      } yield v1 + v2
    }

  (0 until 20) foreach { i =>
    s"fib($i) = ${runtime unsafeRun fib(i)}" pipe println
  }

  // ------------------------------------------------------------
  printTextInLine("Error Model")

  // ------------------------------------------------------------
  printTextInLine("Parallelism: Fiber#zipPar")

  type Matrix = List[List[Int]]

  def computeInverse(m: Matrix): UIO[Matrix] =
    UIO succeed m

  def applyMatrices(m1: Matrix, m2: Matrix, v: Matrix): UIO[Matrix] =
    UIO succeed v

  def bigCompute(m1: Matrix, m2: Matrix, v: Matrix): UIO[Matrix] =
    for {
      t <- computeInverse(m1).zipPar(computeInverse(m2))
      (i1, i2) = t
      r <- applyMatrices(i1, i2, v)
    } yield r

  val bigCompute123: UIO[Matrix] =
    bigCompute(List(List(1)), List(List(2)), List(List(3)))
  (runtime unsafeRun bigCompute123) pipe println

  // ------------------------------------------------------------
  printTextInLine("Racing: Fiber#race, Fiber#raceWith")

  (runtime unsafeRun (fib(20) race fib(10))) pipe println

  printLine()
}
