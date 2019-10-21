package example

import util.formatting._

object Hello extends Greeting with App {

  prtLine()

  println(greeting)

  prtLine()
}

trait Greeting {
  lazy val greeting: String = "hello"
}
