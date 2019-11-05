package howto.accesssysteminfo

import zio._
import zio.console._
import util.formatting._

object AccessSystemInformation extends App {

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {

    (for {
      _             <- putStrLn(title(objectName(this)))
      _             <- putStrLn("--- Environment Variables:")
      javaHome      <- system.env("JAVA_HOME")
      _             <- putStrLn(s"JAVA_HOME=$javaHome")
      _             <- putStrLn("--- Properties:")
      javaVersion   <- system.property("java.version")
      _             <- putStrLn(s"java.version=$javaVersion")
      _             <- putStrLn("--- Miscellaneous:")
      lineSeparator <- system.lineSeparator
      _             <- putStrLn(s"line separator=${lineSeparator.codePointAt(0)}")
      _             <- putStrLn(line())
    } yield ())
      .fold(_ => 1, _ => 0)
  }
}
