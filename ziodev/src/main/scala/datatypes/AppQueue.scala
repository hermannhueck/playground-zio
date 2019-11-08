/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_queue
 */

package datatypes

import util.formatting._
import scala.util.chaining._
import zio._
import zio.clock._
import zio.duration._
import java.util.concurrent.TimeUnit

object AppQueue extends scala.App {

  prtTitleObjectName(this)

  val runtime = new DefaultRuntime {}

  // ------------------------------------------------------------
  prtSubTitle("Queue")

  val res: UIO[Int] = for {
    queue <- Queue.bounded[Int](100)
    _     <- queue.offer(1)
    v1    <- queue.take
  } yield v1

  (runtime unsafeRun res) pipe println

  // ------------------------------------------------------------
  prtSubTitle("Creating a Queue: .bounded, .dropping, .sliding, .unbounded")

  // To create a back-pressured bounded queue:
  val boundedQueue: UIO[Queue[Int]] = Queue.bounded[Int](100)

  // To create a dropping queue:
  val droppingQueue: UIO[Queue[Int]] = Queue.dropping[Int](100)

  // To create a sliding queue:
  val slidingQueue: UIO[Queue[Int]] = Queue.sliding[Int](100)

  // To create an unbounded queue:
  val unboundedQueue: UIO[Queue[Int]] = Queue.unbounded[Int]

  // ------------------------------------------------------------
  prtSubTitle("Adding items to a Queue: #offer, #offerAll")

  val res1: UIO[Unit] = for {
    queue <- Queue.bounded[Int](100)
    _     <- queue.offer(1)
  } yield ()

  (runtime unsafeRun res1) pipe println

  val res2: UIO[Unit] = for {
    queue <- Queue.bounded[Int](1)
    _     <- queue.offer(1)
    f     <- queue.offer(1).fork // will be suspended because the queue is full
    _     <- queue.take
    _     <- f.join
  } yield ()

  (runtime unsafeRun res2) pipe println

  val res3: UIO[Unit] = for {
    queue <- Queue.bounded[Int](100)
    items = Range.inclusive(1, 10).toList
    _     <- queue.offerAll(items)
  } yield ()

  (runtime unsafeRun res3) pipe println

  // ------------------------------------------------------------
  prtSubTitle("Consuming Items from a Queue: #take, #poll, #takeUpTo, takeAll")

  val oldestItem: UIO[String] = for {
    queue <- Queue.bounded[String](100)
    f     <- queue.take.fork // will be suspended because the queue is empty
    _     <- queue.offer("something")
    v     <- f.join
  } yield v

  (runtime unsafeRun oldestItem) pipe println

  val polled: UIO[Option[Int]] = for {
    queue <- Queue.bounded[Int](100)
    _     <- queue.offer(10)
    _     <- queue.offer(20)
    head  <- queue.poll
  } yield head

  (runtime unsafeRun polled) pipe println

  val taken: UIO[List[Int]] = for {
    queue <- Queue.bounded[Int](100)
    _     <- queue.offer(10)
    _     <- queue.offer(20)
    list  <- queue.takeUpTo(5)
  } yield list

  (runtime unsafeRun taken) pipe println

  val all: UIO[List[Int]] = for {
    queue <- Queue.bounded[Int](100)
    _     <- queue.offer(10)
    _     <- queue.offer(20)
    list  <- queue.takeAll
  } yield list

  (runtime unsafeRun all) pipe println

  // ------------------------------------------------------------
  prtSubTitle("Shutting Down a Queue: #shutdown, #awaitShutdown")

  val takeFromShutdownQueue: UIO[Unit] = for {
    queue <- Queue.bounded[Int](3)
    f     <- queue.take.fork
    _     <- queue.shutdown // will interrupt f
    _     <- f.join // Will terminate
  } yield ()

  // produces an error
  // (runtime unsafeRun takeFromShutdownQueue) pipe println

  val awaitShutdown: UIO[Unit] = for {
    queue <- Queue.bounded[Int](3)
    // p     <- Promise.make[Nothing, Boolean]
    f <- queue.awaitShutdown.fork
    _ <- queue.shutdown
    _ <- f.join
  } yield ()

  (runtime unsafeRun awaitShutdown) pipe println

  // ------------------------------------------------------------
  prtSubTitle("Transforming Queues")

  // ------------------------------------------------------------
  prtSubTitle("ZQueue#map")

  val mapped: UIO[String] =
    for {
      queue  <- Queue.bounded[Int](3)
      mapped = queue.map(_.toString)
      _      <- mapped.offer(1)
      s      <- mapped.take
    } yield s

  (runtime unsafeRun mapped) pipe println

  // ------------------------------------------------------------
  prtSubTitle("ZQueue#mapM")

  val currentTimeMillis = currentTime(TimeUnit.MILLISECONDS)

  val annotatedOut_orig: UIO[ZQueue[Any, Nothing, Clock, Nothing, String, (Long, String)]] =
    for {
      queue <- Queue.bounded[String](3)
      mapped = queue.mapM { el =>
        currentTimeMillis.map((_, el))
      }
    } yield mapped

  val annotatedOut: ZIO[Clock, Nothing, (FiberId, String)] =
    for {
      queue <- Queue.bounded[String](3)
      mapped = queue.mapM { el =>
        currentTimeMillis.map((_, el))
      }
      _     <- mapped.offer("hello")
      tuple <- mapped.take
    } yield tuple

  (runtime unsafeRun annotatedOut) pipe println

  // ------------------------------------------------------------
  prtSubTitle("ZQueue#contramapM")

  val annotatedIn_orig: UIO[ZQueue[Clock, Nothing, Any, Nothing, String, (Long, String)]] =
    for {
      queue <- Queue.bounded[(Long, String)](3)
      mapped = queue.contramapM { el: String =>
        currentTimeMillis.map((_, el))
      }
    } yield mapped

  val annotatedIn: ZIO[Clock, Nothing, (FiberId, String)] =
    for {
      queue <- Queue.bounded[(Long, String)](3)
      mapped = queue.contramapM { el: String =>
        currentTimeMillis.map((_, el))
      }
      _     <- mapped.offer("hello")
      tuple <- mapped.take
    } yield tuple

  (runtime unsafeRun annotatedIn) pipe println

  val timeQueued_orig: UIO[ZQueue[Clock, Nothing, Clock, Nothing, String, (Duration, String)]] =
    for {
      queue <- Queue.bounded[(Long, String)](3)
      enqueueTimestamps = queue.contramapM { el: String =>
        currentTimeMillis.map((_, el))
      }
      durations = enqueueTimestamps.mapM {
        case (enqueueTs, el) =>
          currentTimeMillis
            .map(dequeueTs => ((dequeueTs - enqueueTs).millis, el))
      }
    } yield durations

  val timeQueued: ZIO[Clock, Nothing, (Duration, String)] =
    for {
      queue <- Queue.bounded[(Long, String)](3)
      enqueueTimestamps = queue.contramapM { el: String =>
        currentTimeMillis.map((_, el))
      }
      durations = enqueueTimestamps.mapM {
        case (enqueueTs, el) =>
          currentTimeMillis
            .map(dequeueTs => ((dequeueTs - enqueueTs).millis, el))
      }
      _     <- enqueueTimestamps.offer("hello")
      tuple <- durations.take
    } yield tuple

  (runtime unsafeRun timeQueued) pipe println

  // ------------------------------------------------------------
  prtSubTitle("ZQueue#bothWith")

  val fromComposedQueues: UIO[(Int, String)] =
    for {
      q1       <- Queue.bounded[Int](3)
      q2       <- Queue.bounded[Int](3)
      q2Mapped = q2.map(_.toString)
      both     = q1.bothWith(q2Mapped)((_, _))
      _        <- both.offer(1)
      iAndS    <- both.take
      (i, s)   = iAndS
    } yield (i, s)

  (runtime unsafeRun fromComposedQueues) pipe println

  prtLine()
}
