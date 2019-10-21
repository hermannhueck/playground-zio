package scala.util

import scala.language.reflectiveCalls

object Using {

  type Closable = { def close(): Unit }

  def apply[A, R <: Closable](resrc: R)(use: R => A): Try[A] =
    Try(resource(resrc)(use))

  def resource[A, R <: Closable](resrc: R)(use: R => A): A =
    try {
      use(resrc)
    } finally {
      resrc.close()
    }
}
