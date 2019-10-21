package scala.util

package object chaining {

  implicit class ChainingOps[A](private val a: A) extends AnyVal {

    @inline def pipe[B](f: A => B): B   = f(a)
    @inline def tap[B](f: A => Unit): A = { f(a); a }
  }
}
