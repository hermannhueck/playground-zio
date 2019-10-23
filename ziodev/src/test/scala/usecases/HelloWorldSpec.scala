/*
  See docs at:
  https://zio.dev/docs/usecases/usecases_testing
 */

package usecases

import zio._
import zio.console._
import zio.test._
import zio.test.Assertion._
import zio.test.environment._

object HelloWorld {

  def sayHello: ZIO[Console, Nothing, Unit] =
    console.putStrLn("Hello, World!")
}

import HelloWorld._

object HelloWorldSpec
    extends DefaultRunnableSpec(
      suite("HelloWorldSpec")(
        testM("sayHello correctly displays output") {
          for {
            _      <- sayHello
            output <- TestConsole.output
          } yield assert(output, equalTo(Vector("Hello, World!\n")))
        }
      )
    )
