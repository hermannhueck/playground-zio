package overview

package object ch04handlingresources {

  import java.io.BufferedReader

  def wordCount(words: Seq[String]): Seq[(String, Int)] =
    words
      .map(w => w -> 1)
      .groupBy(_._1)
      // .view // MapView not supported in 2.12
      .mapValues(_.length)
      .filter { case (k, v) => v > 1 } // omit words with just 1 occurrence
      .filterNot { case (k, v) => k.matches("^\\d.*") } // omit words starting with a digit
      .toList
      .sortWith { // sort 1st by occurence desc and then alphabetically
        case ((w1, c1), (w2, c2)) => // -> syntax not supported in 2.12 pattern match
          if (c1 == c2)
            w1 < w2
          else
            c2 < c1
      }
}
