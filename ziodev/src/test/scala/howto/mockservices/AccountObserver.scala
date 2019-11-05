package howto.mockservices

import zio._
// import zio.test.mock._
import zio.macros.annotation.accessible
import zio.console.Console

// @mockable
@accessible
trait AccountObserver {
  val accountObserver: AccountObserver.Service[Any]
}

trait AccountEvent

object AccountObserver {

  trait Service[R] {
    def processEvent(event: AccountEvent): ZIO[R, Nothing, Unit]
  }

  // autogenerated `object Service { ... }`
  // autogenerated `object > extends Service[AccountObserver] { ... }`
  // autogenerated `implicit val mockable: Mockable[AccountObserver] = ...`
}

trait AccountObserverLive extends AccountObserver {

  // dependency on Console module
  val console: Console.Service[Any]

  val accountObserver = new AccountObserver.Service[Any] {

    def processEvent(event: AccountEvent): UIO[Unit] =
      for {
        _    <- console.putStrLn(s"Got $event")
        line <- console.getStrLn.orDie
        _    <- console.putStrLn(s"You entered: $line")
      } yield ()
  }
}
