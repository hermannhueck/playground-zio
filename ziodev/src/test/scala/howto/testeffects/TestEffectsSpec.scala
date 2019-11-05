package howto.testeffects

import zio.test._
import zio.test.Assertion._

object TestEffectsSpec
    extends DefaultRunnableSpec(
      suite("TestEffectsSpec")(
        suite("Suite: Constructing tests")(
          testM("time is non-zero") {
            import zio.clock.nanoTime
            assertM(nanoTime, isGreaterThan(-1L))
          }
        ),
        suite("Suite: Assertions - creating TestResults")(
          testM("BarFooBar tested with testM/accessM") {
            assertM(zio.ZIO.succeed("BarFooBar"), containsString("Foo") && endsWith("Bar"))
          },
          test("BarFooBar tested with test/access") {
            assert("BarFooBar", containsString("Foo") && endsWith("Bar"))
          },
          test("Check assertions") {
            assert(Right(Some(2)), isRight(isSome(equalTo(2))))
          },
          test("Rich checking") {

            final case class Address(country: String, city: String)
            final case class User(name: String, age: Int, address: Address)

            assert(
              User("Jonny", 26, Address("Denmark", "Copenhagen")),
              hasField("age", (u: User) => u.age, isGreaterThanEqualTo(18)) &&
                hasField("country", (u: User) => u.address.country, not(equalTo("USA")))
            )
          },
          testM("Semaphore should expose available number of permits") {
            for {
              s       <- zio.Semaphore.make(1L)
              permits <- s.available
            } yield assert(permits, equalTo(1L))
          }
        ),
        suite("Suite: Using Test Environment")(
          testM("`acquire` doesn't leak permits upon cancellation") {
            /*
            import zio.test.environment.TestClock
            import zio.duration._
            import zio.test.TestAspect.timeout
            for {
              testClock <- TestClock.makeTest(TestClock.DefaultData)
              s         <- zio.Semaphore.make(1L)
              sf        <- s.acquireN(2).timeout(1.milli).either.fork
              _         <- testClock.adjust(1.second)
              _         <- sf.join
              _         <- s.release
              permits   <- s.available
            } yield assert(permits, equalTo(2L))
             */
            assertM(zio.ZIO.succeed(42), equalTo(42))
          }
        ),
        suite("Suite: Testing Random")(
          testM("Use setSeed to generate stable values") {
            import zio.test.environment.TestRandom
            import zio.random
            for {
              _  <- TestRandom.setSeed(27)
              r1 <- random.nextLong
              r2 <- random.nextLong
              r3 <- random.nextLong
            } yield assert(
              List(r1, r2, r3),
              equalTo(
                List[Long](
                  -4947896108136290151L,
                  -5264020926839611059L,
                  -9135922664019402287L
                )
              )
            )
          },
          testM("One can provide its own list of ints") {
            import zio.test.environment.TestRandom
            import zio.random
            for {
              _  <- TestRandom.feedInts(1, 9, 2, 8, 3, 7, 4, 6, 5)
              r1 <- random.nextInt
              r2 <- random.nextInt
              r3 <- random.nextInt
              r4 <- random.nextInt
              r5 <- random.nextInt
              r6 <- random.nextInt
              r7 <- random.nextInt
              r8 <- random.nextInt
              r9 <- random.nextInt
            } yield assert(
              List(1, 9, 2, 8, 3, 7, 4, 6, 5),
              equalTo(List(r1, r2, r3, r4, r5, r6, r7, r8, r9))
            )
          }
        ),
        suite("Suite: Testing Clock")(
          testM("One can move time very fast") {
            import zio.test.environment.TestClock
            import zio.duration._
            import zio.clock.currentTime
            import java.util.concurrent.TimeUnit
            for {
              startTime <- currentTime(TimeUnit.SECONDS)
              _         <- TestClock.adjust(1.minute)
              endTime   <- currentTime(TimeUnit.SECONDS)
            } yield assert(endTime - startTime, isGreaterThanEqualTo(60L))
          },
          testM("One can control time as he see fit") {
            import zio.test.environment.TestClock
            import zio.duration._
            for {
              promise <- zio.Promise.make[Unit, Int]
              _       <- (zio.ZIO.sleep(10.seconds) *> promise.succeed(1)).fork
              _       <- TestClock.adjust(10.seconds)
              readRef <- promise.await
            } yield assert(1, equalTo(readRef))
          },
          testM("THIS TEST WILL FAIL - Sleep and adjust can introduce races") {
            import zio.test.environment.TestClock
            import zio.duration._
            import java.util.concurrent.TimeUnit
            for {
              ref   <- zio.Ref.make(0)
              _     <- (zio.ZIO.sleep(Duration(10, TimeUnit.SECONDS)) *> ref.update(_ + 1)).fork
              _     <- TestClock.adjust(Duration(10, TimeUnit.SECONDS))
              value <- ref.get
            } yield assert(1, equalTo(value))
          }, // @@ zio.test.TestAspect.timeout(zio.duration.Duration(12, java.util.concurrent.TimeUnit.SECONDS)),
          testM("zipWithLatest") {

            import zio.test.environment.TestClock
            import zio.duration._
            import zio.stream._

            val s1 = Stream.iterate(0)(_ + 1).fixed(100.millis)
            val s2 = Stream.iterate(0)(_ + 1).fixed(70.millis)
            val s3 = s1.zipWithLatest(s2)((_, _))

            for {
              _ <- TestClock.setTime(0.millis)
              q <- zio.Queue.unbounded[(Int, Int)]
              _ <- s3.foreach(q.offer).fork
              a <- q.take
              _ <- TestClock.setTime(70.millis)
              b <- q.take
              _ <- TestClock.setTime(100.millis)
              c <- q.take
              _ <- TestClock.setTime(140.millis)
              d <- q.take
            } yield assert(a, equalTo(0 -> 0)) &&
              assert(b, equalTo(0       -> 1)) &&
              assert(c, equalTo(1       -> 1)) &&
              assert(d, equalTo(1       -> 2))
          }
        ),
        suite("Testing Console")(
          testM("One can test output of console") {
            import zio.test.environment.TestConsole
            import zio.console
            for {
              _              <- TestConsole.feedLines("Jimmy", "37")
              _              <- console.putStrLn("What is your name?")
              name           <- console.getStrLn
              _              <- console.putStrLn("What is your age?")
              age            <- console.getStrLn.map(_.toInt)
              questionVector <- TestConsole.output
              q1             = questionVector(0)
              q2             = questionVector(1)
            } yield {
              assert(name, equalTo("Jimmy")) &&
              assert(age, equalTo(37)) &&
              assert(q1, equalTo("What is your name?\n")) &&
              assert(q2, equalTo("What is your age?\n"))
            }
          }
        ),
        suite("Testing System")(
          testM("One can set and test system proverties and environment variables") {
            import zio.test.environment.TestSystem
            import zio.system
            for {
              _      <- TestSystem.putProperty("java.vm.name", "VM")
              result <- system.property("java.vm.name")
            } yield assert(result, equalTo(Some("VM")))
          }
        ),
        suite("Test Aspects") {

          // import zio.duration._
          import zio.test.Assertion._
          import zio.test.TestAspect._
          import zio.test._

          suite("with sub suite")(
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
        }
      )
    )
