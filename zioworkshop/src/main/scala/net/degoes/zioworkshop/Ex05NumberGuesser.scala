package net.degoes.zioworkshop

import zio._
import zio.console._
import zio.random._

object Ex05NumberGuesser extends App {

  def analyzeAnswer(random: Int, guess: String): URIO[Console, Boolean] =
    if (random.toString == guess.trim)
      putStrLn(s"${scala.Console.GREEN}You guessed correctly: $random ${scala.Console.RESET}") *>
        IO.succeed(true)
    else
      putStrLn(
        s"${scala.Console.RED}You did not guess correctly. You guessed $guess. The answer was ${random}${scala.Console.RESET}"
      ) *>
        IO.succeed(false)

  val maxNumber = 5

  /**
    * EXERCISE 5
    *
    * Choose a random number (using `nextInt`), and then ask the user to guess
    * the number, feeding their response to `analyzeAnswer`, above.
    */

  def runGuess: RIO[Random with Console, Boolean] =
    for {
      randomNumber <- nextInt(maxNumber)
      _            <- putStrLn(s"\nGuess a number between 0 and ${maxNumber - 1}:  ")
      guess        <- getStrLn
      guessed      <- analyzeAnswer(randomNumber, guess)
    } yield guessed

  def repeatGuess: RIO[Random with Console, Unit] =
    for {
      guessedCorrectly <- runGuess
      _ <- if (guessedCorrectly)
            IO.succeed(())
          else
            repeatGuess
    } yield ()

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    withLines {
      repeatGuess
        .fold(_ => 1, _ => 0)
    }
}
