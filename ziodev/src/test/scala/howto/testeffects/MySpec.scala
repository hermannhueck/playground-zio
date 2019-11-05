package howto.testeffects

// import zio.duration._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

object MySpec
    extends DefaultRunnableSpec(
      suite("A Suite")(
        test("A passing test") {
          assert(true, isTrue)
        },
        test("A passing test run for JVM only") {
          assert(true, isTrue)
        } @@ jvmOnly, //@@ jvmOnly only runs tests on the JVM
        test("A passing test run for JS only") {
          assert(true, isTrue)
        } @@ jsOnly, //@@ jsOnly only runs tests on Scala.js
        test("A passing test with a timeout") {
          assert(true, isTrue)
        }, // @@ timeout(10.nanos), //@@ timeout will fail a test that doesn't pass within the specified time
        test("A failing test... that passes") {
          assert(true, isFalse)
        } @@ failure, //@@ failure turns a failing test into a passing test
        test(
          "A flaky test that only works on the JVM and sometimes fails; let's compose some aspects!"
        ) {
          assert(false, isTrue)
        } @@ jvmOnly // only run on the JVM
        // @@ eventually //@@ eventually retries a test indefinitely until it succeeds
        // @@ timeout(20.nanos) //it's a good idea to compose `eventually` with `timeout`, or the test may never end
      ) // @@ timeout(60.seconds) //apply a timeout to the whole suite
    )
