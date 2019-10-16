package overview.ch08background

object ConsoleExample2 extends App {

  def succeed[A](a: => A): Console[A] =
    Return(() => a)
  def printLine(line: String): Console[Unit] =
    PrintLine(line, succeed(()))
  val readLine: Console[String] =
    ReadLine(line => succeed(line))

  implicit class ConsoleSyntax[+A](self: Console[A]) {

    def map[B](f: A => B): Console[B] =
      flatMap(a => succeed(f(a)))
  
    def flatMap[B](f: A => Console[B]): Console[B] =
      self match {
        case Return(value) =>
          f(value())
        case PrintLine(line, next) =>
          PrintLine(line, next.flatMap(f))
        case ReadLine(next) =>
          ReadLine(line => next(line).flatMap(f))
      }
  }

  val example2: Console[String] =
    for {
      _    <- printLine("What's your name?")
      name <- readLine
      _    <- printLine(s"Hello, ${name}, good to meet you!")
    } yield name

  import Console._
  interpret(example2)
}
