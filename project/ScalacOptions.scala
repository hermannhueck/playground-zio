import sbt._

object ScalacOptions {

  val scalacOptionsForAllVersions = Seq(
    "-encoding",
    "UTF-8",        // source files are in UTF-8
    "-deprecation", // warn about use of deprecated APIs
    "-unchecked",   // warn about unchecked type parameters
    "-feature",     // warn about misused language features
    // "-Xfatal-warnings", // fail the compilation if there are any warnings
    "-explaintypes", // explain type errors in more detail
    "-Xcheckinit"    // wrap field accessors to throw an exception on uninitialized access
  )

  lazy val scalacOptions213 = scalacOptionsForAllVersions ++
    Seq(
      "-Xlint:-unused,_" // suppress unused warnings in 2.13
      // "-Xlint"
    )

  lazy val scalacOptions212 = scalacOptionsForAllVersions ++
    Seq(
      "-Ypartial-unification", // (removed in scala 2.13) allow the compiler to unify type constructors of different arities
      "-language:higherKinds", // (not required since scala 2.13.1) suppress warnings when using higher kinded types
      "-Xlint"                 // enable handy linter warnings
    )

  def scalacOptionsFor(scalaVersion: String): Seq[String] = {
    // println(s"\n>>>>>          compiling for Scala $scalaVersion\n")
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, minor)) if minor >= 13 =>
        scalacOptions213
      case _ =>
        scalacOptions212
    }
  }
}
