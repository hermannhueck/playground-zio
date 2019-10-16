package overview.ch08background

sealed trait Console[+A] extends Product with Serializable
final case class Return[A](value: () => A)                    extends Console[A]
final case class PrintLine[A](line: String, rest: Console[A]) extends Console[A]
final case class ReadLine[A](rest: String => Console[A])      extends Console[A]

object Console {

  def interpret[A](program: Console[A]): A = program match {
    case Return(value) => 
      value()
    case PrintLine(line, next) => 
      println(line)
      interpret(next)
    case ReadLine(next) =>
      interpret(next(scala.io.StdIn.readLine()))
    }
}