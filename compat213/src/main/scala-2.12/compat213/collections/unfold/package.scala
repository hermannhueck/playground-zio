package compat213.collections

package object unfold {

  def unfoldToStream[A, B](init: A)(f: A => Option[(B, A)]): Stream[B] =
    f(init)
      .map {
        case (b, a) =>
          b #:: unfoldToStream(a)(f)
      }
      .getOrElse(Stream.empty)

  def unfoldToList[A, B](init: A)(f: A => Option[(B, A)]): List[B] =
    unfoldToStream(init)(f).toList

  // format: off
  implicit class StreamCompanionOps(private val self: Stream.type) extends AnyVal {
    @inline def unfold[A, B](init: A)(f: A => Option[(B, A)]): Stream[B] =
      unfoldToStream(init)(f)
  }

  implicit class ListCompanionOps(private val self: List.type) extends AnyVal {
    @inline def unfold[A, B](init: A)(f: A => Option[(B, A)]): List[B] =
      unfoldToList(init)(f)
  }

  implicit class IteratorCompanionOps(private val self: Iterator.type) extends AnyVal {
    @inline def unfold[A, B](init: A)(f: A => Option[(B, A)]): Iterator[B] =
      unfoldToList(init)(f).toIterator
  }

  implicit class IterableCompanionOps(private val self: Iterable.type) extends AnyVal {
    @inline def unfold[A, B](init: A)(f: A => Option[(B, A)]): Iterable[B] =
      unfoldToList(init)(f).toIterable
  }
  // format: on
}
