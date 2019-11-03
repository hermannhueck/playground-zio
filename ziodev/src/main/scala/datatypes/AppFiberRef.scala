/*
  See docs at:
  https://zio.dev/docs/datatypes/datatypes_fiberref
 */

package datatypes

import zio._
import util.formatting._
import scala.util.chaining._

object AppFiberRef extends scala.App {

  prtTitleObjectName(this)

  // ------------------------------------------------------------
  prtSubTitle("FiberRef: FiberRef.make, FiberRef#get, FiberRef#set")

  val runtime = new DefaultRuntime {}

  val uio1 = for {
    fiberRef <- FiberRef.make[Int](0)
    _        <- fiberRef.set(10)
    v        <- fiberRef.get
  } yield v == 10

  runtime
    .unsafeRun(uio1)
    .ensuring { _ == true } pipe println

  // ------------------------------------------------------------
  prtSubTitle(
    "Operations: FiberRef#update, FiberRef#updateSome, FiberRef#modify, FiberRef#modifySome, FiberRef#locally"
  )

  val uio2: UIO[Boolean] = for {
    correlationId <- FiberRef.make[String]("")
    v1            <- correlationId.locally("my-correlation-id")(correlationId.get)
    v2            <- correlationId.get
  } yield v1 == "my-correlation-id" && v2 == ""

  runtime
    .unsafeRun(uio2)
    .ensuring { _ == true } pipe println

  // ------------------------------------------------------------
  prtSubTitle("Propagation")

  "--- FiberRef[A] has copy-on-fork semantics for ZIO#fork. ---" pipe println

  val uio3 = for {
    fiberRef <- FiberRef.make[Int](0)
    _        <- fiberRef.set(10)
    child    <- fiberRef.get.fork
    v        <- child.join
  } yield v == 10

  runtime
    .unsafeRun(uio3)
    .ensuring { _ == true } pipe println

  "--- Fiber#inheritFiberRefs ---" pipe println

  val uio4 = for {
    fiberRef <- FiberRef.make[Int](0)
    latch    <- Promise.make[Nothing, Unit]
    fiber    <- (fiberRef.set(10) *> latch.succeed(())).fork
    _        <- latch.await
    _        <- fiber.inheritFiberRefs
    v        <- fiberRef.get
  } yield v == 10

  runtime
    .unsafeRun(uio4)
    .ensuring { _ == true } pipe println

  "--- same semantics with and without join ---" pipe println

  val withJoin =
    for {
      fiberRef <- FiberRef.make[Int](0)
      fiber    <- fiberRef.set(10).fork
      _        <- fiber.join
      v        <- fiberRef.get
    } yield v == 10

  runtime
    .unsafeRun(withJoin)
    .ensuring { _ == true } pipe println

  val withoutJoin =
    for {
      fiberRef <- FiberRef.make[Int](0)
      fiber    <- fiberRef.set(10)
      v        <- fiberRef.get
    } yield v == 10

  runtime
    .unsafeRun(withoutJoin)
    .ensuring { _ == true } pipe println

  "--- customized merge of 2 FiberRef values ---" pipe println

  val uioMax = for {
    fiberRef <- FiberRef.make(0, math.max)
    child    <- fiberRef.update(_ + 1).fork
    _        <- fiberRef.update(_ + 2)
    _        <- child.join
    value    <- fiberRef.get
  } yield value == 2

  runtime
    .unsafeRun(uioMax)
    .ensuring { _ == true } pipe println

  // ------------------------------------------------------------
  prtSubTitle("Memory Safety")

  "The value of a FiberRef is automatically garbage collected once the Fiber owning it is finished." pipe println

  prtLine()
}
