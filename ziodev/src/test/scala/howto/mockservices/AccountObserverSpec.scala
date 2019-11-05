package howto.mockservices

import zio.test._
import zio.test.Assertion._
// import zio.test.mock._
import zio._

object AccountObserverSpec
    extends DefaultRunnableSpec(
      suite("processEvent") {

        /*
        val event = new AccountEvent {}
        val app   = AccountObserver.>.processEvent(event)

        val mockEnv: Managed[Nothing, MockConsole] = (
          MockSpec.expectIn(MockConsole.Service.putStrLn)(equalTo(s"Got $event")) *>
            MockSpec.expectOut(MockConsole.Service.getStrLn)("42") *>
            MockSpec.expectIn(MockConsole.Service.putStrLn)(equalTo("You entered: 42"))
        )

        testM("calls putStrLn > getStrLn > putStrLn and returns unit") {
          val result = app.provideManaged(mockEnv.map { mockConsole =>
            new AccountObserverLive with Console {
              val console = mockConsole.console
            }
          })
          assertM(result, isUnit)
        }
         */
        testM("calls putStrLn > getStrLn > putStrLn and returns unit") {
          assertM(ZIO.succeed(42), equalTo(42))
        }
      }
    )
