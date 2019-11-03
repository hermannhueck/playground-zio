package net.degoes.zioworkshop

import zio._
import zio.console._

object Ex01aHelloWorld extends App {

  /**
    * EXERCISE 1
    *
    * Implement a simple "Hello World" program using the effect returned by `putStrLn`.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    withLines {
      putStrLn("Hello World!")
      // .as(0)
        .fold(_ => 1, _ => 0)
    }
}
