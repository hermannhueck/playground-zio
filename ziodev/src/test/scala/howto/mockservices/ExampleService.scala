package howto.mockservices

import zio.ZIO
import zio.test.mock._

trait ExampleService {
  val exampleService: ExampleService.Service[Any]
}

object ExampleService {

  trait Service[R] {
    val static: ZIO[R, Nothing, String]
    def zeroArgs: ZIO[R, Nothing, Int]
    def zeroArgsWithParens(): ZIO[R, Nothing, Long]
    def singleArg(arg1: Int): ZIO[R, Nothing, String]
    def multiArgs(arg1: Int, arg2: Long): ZIO[R, Nothing, String]
    def multiParamLists(arg1: Int)(arg2: Long): ZIO[R, Nothing, String]
    def command(arg1: Int): ZIO[R, Nothing, Unit]
    def overloaded(arg1: Int): ZIO[R, Nothing, String]
    def overloaded(arg1: Long): ZIO[R, Nothing, String]
  }

  object Service {
    object static             extends Method[ExampleService, Unit, String]
    object zeroArgs           extends Method[ExampleService, Unit, Int]
    object zeroArgsWithParens extends Method[ExampleService, Unit, Long]
    object singleArg          extends Method[ExampleService, Int, String]
    object multiArgs          extends Method[ExampleService, (Int, Long), String]
    object multiParamLists    extends Method[ExampleService, (Int, Long), String]
    object command            extends Method[ExampleService, Int, Unit]
  }

  object overloaded {
    object _1 extends Method[ExampleService, Int, String]
    object _2 extends Method[ExampleService, Long, String]
  }
}
