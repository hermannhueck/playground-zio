/*
  See docs at:
  https://zio.dev/docs/usecases/usecases_testing
 */

package usecases

import zio.test._
import zio.test.Assertion._

object AssociativitySpec
    extends DefaultRunnableSpec(
      suite("AssociativitySpec")(
        testM("Inter addition is associative") {
          check(Gen.anyInt, Gen.anyInt, Gen.anyInt) { (x, y, z) =>
            assert((x + y) + z, equalTo(x + (y + z)))
          }
        }
      )
    )
