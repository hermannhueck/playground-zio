package howto.mockservices

import zio._
import zio.test.mock._

object CreatingAMockService {

  trait AccountObserver {
    val accountObserver: AccountObserver.Service[Any]
  }

  trait AccountEvent

  object AccountObserver {

    trait Service[R] {
      def processEvent(event: AccountEvent): ZIO[R, Nothing, Unit]
    }

    object Service {
      object processEvent extends Method[AccountObserver, AccountEvent, Unit]
    }

    object > extends Service[AccountObserver] {

      def processEvent(event: AccountEvent) =
        ZIO.accessM(_.accountObserver.processEvent(event))
    }

    implicit val mockable: Mockable[AccountObserver] = (mock: Mock) =>
      new AccountObserver {

        val accountObserver = new AccountObserver.Service[Any] {

          def processEvent(event: AccountEvent): UIO[Unit] =
            mock(AccountObserver.Service.processEvent, event)
        }
      }
  }
}
