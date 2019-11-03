package net.degoes.zioworkshop

import zio._
import zio.console._

object Ex01bHelloWorld extends App {

  /**
    * EXERCISE 1
    *
    * Implement a simple "Hello World" program using the effect returned by `putStrLn`.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    withLines {
      putStrLn("Hello World!")
        .as(0)
        .orElse(IO.succeed(1))
    }
}
