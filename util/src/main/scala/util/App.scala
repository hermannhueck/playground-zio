package util

import java.lang.System.{currentTimeMillis => currentTime}
import scala.collection.mutable.ListBuffer
// import scala.util.Using
import util.formatting._

trait App extends DelayedInit {

  type Closable = { def close(): Unit }

  def resource[A, R <: Closable](resrc: R)(use: R => A): A =
    try {
      use(resrc)
    } finally {
      resrc.close()
    }

  final val executionStart: Long = currentTime

  protected final def args: Array[String] = _args

  private[this] var _args: Array[String] = _

  private[this] val initCode = new ListBuffer[() => Unit]

  /*
  def execBody(body: => Unit): Unit =
    Using.resource[Unit, Unit] {
      printHeaderWithProgramName(this)
    } { _ =>
      body
    } { _ =>
      val total = currentTime - executionStart
      printFooter(s"[total: $total ms]")
    }
   */

  def execBody(body: => Unit): Unit =
    try {
      // printHeaderWithProgramName(this)
      printHeaderWithProgramName(this)
      body
    } finally {
      val total = currentTime - executionStart
      printFooter(s"[total: $total ms]")
    }

  override def delayedInit(body: => Unit): Unit = {
    initCode += (() => execBody(body))
  }

  final def main(args: Array[String]) = {
    this._args = args
    for (proc <- initCode) proc()
  }
}
