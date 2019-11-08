package overview

package object ch04handlingresources {

  def wordCount(words: Seq[String]): Seq[(String, Int)] =
    words
      .map(w => w -> 1)
      .groupBy(_._1)
      .view
      .mapValues(_.length)
      .filter { case _ -> v => v > 1 } // omit words with just 1 occurrence
      .filterNot { case k -> _ => k.matches("^\\d.*") } // omit words starting with a digit
      .toList
      .sortWith { // sort 1st by occurence desc and then alphabetically
        case (w1 -> c1, w2 -> c2) =>
          if (c1 == c2)
            w1 < w2
          else
            c2 < c1
      }
}
