package net.degoes.zioworkshop

import zio._
import zio.random._
import zio.console._
import util.formatting._

object Ex12b2ComputePiInParallel extends App {

  /**
    * Some state to keep track of all points inside a circle,
    * and total number of points.
    */
  final case class PiState(
      inside: Ref[Long],
      total: Ref[Long]
  )

  val zioPiState: ZIO[ZEnv, Nothing, PiState] = for {
    inside <- Ref.make(0L)
    total  <- Ref.make(0L)
  } yield PiState(inside, total)

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
  def computeNextPoint: ZIO[ZEnv, Nothing, Unit] =
    for {
      piState <- zioPiState
      tuple   <- randomPoint
      (x, y)  = tuple
      _       <- piState.total.update(_ + 1)
      _       <- incrementIfInsideCircle(piState.inside, x, y)
      total2  <- piState.total.get
      _       <- putStrLn(s"current total = $total2")
    } yield ()

  def incrementIfInsideCircle(ref: Ref[Long], x: Double, y: Double): ZIO[Any, Nothing, AnyVal] =
    if (insideCircle(x, y))
      ref.update(_ + 1)
    else
      IO.unit

  def comnputeWithFibers(n: Long): ZIO[ZEnv, Nothing, Unit] =
    if (n <= 0)
      UIO.unit
    else
      ZIO sequence { // List[ZIO[..., Fiber]] --> ZIO[..., List[Fiber]]
        (0L until n)
          .toList
          .map(_ => computeNextPoint.fork)
      } flatMap Fiber.awaitAll

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      _       <- putStrLn(title(objectName(this)))
      _       <- comnputeWithFibers(10)
      piState <- zioPiState
      inside  <- piState.inside.get
      total   <- piState.total.get
      _       <- putStrLn(s"\ninside = $inside, total = $total")
      pi      = estimatePi(inside, total)
      _       <- putStrLn(s"PI (total/inside) = $pi")
      _       <- putStrLn(line())
    } yield ())
      .fold(_ => 1, _ => 0)
}
