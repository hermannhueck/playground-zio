package example

import util.formatting._

object Hello extends Greeting with App {

  printLine()

  println(greeting)

  printLine()
}

trait Greeting {
  lazy val greeting: String = "hello"
}
