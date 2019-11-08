package net.degoes.zioworkshop

import zio._
import zio.random._
import zio.console._
import util.formatting._

object Ex12aComputePiSequentially extends App {

  /**
    * Some state to keep track of all points inside a circle,
    * and total number of points.
    */
  final case class PiState(
      inside: Ref[Long],
      total: Ref[Long]
  )

  /**
    * A function to estimate pi.
    */
  def estimatePi(inside: Long, total: Long): Double =
    (inside.toDouble / total.toDouble) * 4.0

  /**
    * A helper function that determines if a point lies in
    * a circle of 1 radius.
    */
  def insideCircle(x: Double, y: Double): Boolean =
    Math.sqrt(x * x + y * y) <= 1.0

  /**
    * An effect that computes a random (x, y) point.
    */
  val randomPoint: ZIO[Random, Nothing, (Double, Double)] =
    nextDouble zip nextDouble

  /**
    * EXERCISE 12
    *
    * Build a multi-fiber program that estimates the value of `pi`. Print out
    * ongoing estimates continuously until the estimation is complete.
    */
  def computeNextPoint(pst: PiState): ZIO[ZEnv, Nothing, PiState] =
    for {
      tuple <- randomPoint
      (x, y) = tuple
      _ <- pst.total.update(_ + 1)
      _ <- if (insideCircle(x, y))
        pst.inside.update(_ + 1)
      else
        IO.unit
    } yield pst

  def repeat(n: Long)(
      zioPiState: ZIO[ZEnv, Nothing, PiState]): ZIO[ZEnv, Nothing, PiState] =
    if (n <= 0)
      zioPiState
    else
      zioPiState flatMap { state =>
        repeat(n - 1)(computeNextPoint(state))
      }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {

    val initialState: ZIO[ZEnv, Nothing, PiState] = for {
      inside <- Ref.make(0L)
      total <- Ref.make(0L)
    } yield PiState(inside, total)

    val zioEstimate = for {
      finalState <- repeat(1000000)(initialState)
      inside <- finalState.inside.get
      total <- finalState.total.get
      _ <- putStrLn(s"\ninside = $inside, total = $total")
    } yield estimatePi(inside, total)

    (for {
      _ <- putStrLn(header(objectName(this)))
      pi <- zioEstimate
      _ <- putStrLn(s"PI (total/inside) = $pi")
      _ <- putStrLn(line())
    } yield ())
      .fold(_ => 1, _ => 0)
  }
}
