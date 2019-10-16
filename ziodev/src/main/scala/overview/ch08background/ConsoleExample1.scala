package overview.ch08background

object ConsoleExample1 extends App {

  val example1: Console[Unit] = 
    PrintLine("Hello, what is your name?",
      ReadLine { name =>
        PrintLine(s"Good to meet you, ${name}", Return(() => ()))
      }
    )

  import Console.interpret
  
  interpret(example1)
}
