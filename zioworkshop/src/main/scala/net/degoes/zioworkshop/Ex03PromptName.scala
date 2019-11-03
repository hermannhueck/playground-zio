package net.degoes.zioworkshop

import zio._
import zio.console._

object Ex03PromptName extends App {

  val StdInputFailed = 1

  /**
    * EXERCISE 3
    *
    * Implement a simple program that asks the user for their name (using
    * `getStrLn`), and then prints it out to the user (using `putStrLn`).
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = withLines {
    (for {
      _    <- putStrLn("What's your name?  ")
      name <- getStrLn
      -    <- putStrLn(s"Hello $name!")
    } yield ())
      .fold(_ => StdInputFailed, _ => 0)
  }
}
