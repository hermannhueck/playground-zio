package scala.util

package object chaining {

  implicit class ChainingOps[A](private val self: A) extends AnyVal {
    @inline def pipe[B](f: A => B): B   = f(self)
    @inline def tap[B](f: A => Unit): A = self pipe (x => { f(x); x })
  }
}
