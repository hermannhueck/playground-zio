package example

import util._

object Hello extends Greeting with App {

  prtLine()

  println(greeting)

  prtLine()
}

trait Greeting {
  lazy val greeting: String = "hello"
}
