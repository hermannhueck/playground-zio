package net.degoes.zioworkshop

import zio._
import zio.console._

object Ex02aErrorConversion extends App {

  val StdInputFailed = 1

  val failed =
    putStrLn("About to fail...") *>
      ZIO.fail("Uh oh!") *>
      putStrLn("This will NEVER be printed!")

  /**
    * EXERCISE 2
    *
    * Using `ZIO#orElse` or `ZIO#fold`, have the `run` function compose the
    * preceding `failed` effect into the effect that `run` returns.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    withLines {
      failed
        .fold(_ => StdInputFailed, _ => 0)
    }
}
