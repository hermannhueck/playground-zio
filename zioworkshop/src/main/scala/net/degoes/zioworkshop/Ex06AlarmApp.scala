package net.degoes.zioworkshop

import zio._
import zio.console._
import zio.duration._
import java.io.IOException
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import java.util.concurrent.TimeUnit

object Ex06AlarmApp extends App {

  /**
    * EXERCISE 6
    *
    * Create an effect that will get a `Duration` from the user, by prompting
    * the user to enter a decimal number of seconds.
    */
  lazy val getAlarmDuration: ZIO[Console, IOException, Duration] = {

    def parseDuration(input: String): IO[NumberFormatException, Duration] =
      Try(java.lang.Double.parseDouble(input)) match {
        case Failure(exception) =>
          exception match {
            case nfe: NumberFormatException => IO.fail(nfe)
            case t                          => throw t
          }
        case Success(value) =>
          val millis = (value * 1000).toLong
          IO.succeed(Duration(millis, TimeUnit.MILLISECONDS))
      }

    def fallback(input: String): ZIO[Console, IOException, Duration] =
      parseDuration(input).fold(nfe => Duration(3000, TimeUnit.MILLISECONDS), identity)

    for {
      _        <- putStrLn("Please enter a decimal number to be used as a duration (in seconds):")
      input    <- getStrLn
      duration <- fallback(input)
    } yield duration
  }

  val program =
    for {
      _        <- putStrLn("How long shall I sleep?")
      duration <- getAlarmDuration
      millis   = duration.toMillis
      seconds  = s"${millis / 1000}.${millis % 1000}"
      _        <- putStrLn(s"Sleeping for $seconds seconds ...")
      _        <- ZIO.sleep(duration)
      _        <- putStrLn(s"Woke up after $seconds seconds!")
    } yield 0

  /**
    * EXERCISE 7
    *
    * Create a program that asks the user for a number of seconds to sleep,
    * sleeps the specified number of seconds, and then prints out a wakeup
    * alarm message.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    withLines {
      program orElse IO.succeed(1)
    }
}
