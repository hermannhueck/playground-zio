import sbt._

object ScalacOptions {

  val scalacOptionsForAllVersions = Seq(
    // "-Xfatal-warnings",       // fail the compilation if there are any warnings
    "-encoding",
    "UTF-8",                     // source files are in UTF-8
    "-deprecation",              // warn about use of deprecated APIs
    "-unchecked",                // warn about unchecked type parameters
    "-feature",                  // warn about misused language features
    "-explaintypes",             // explain type errors in more detail
    "-opt-warnings",             // enable optimizer warnings
    "-opt:l:inline",             // enable inline optimizations ...
    "-opt-inline-from:<source>", // ... from source files
    "-Xcheckinit",               // wrap field accessors to throw an exception on uninitialized access
    "-Ywarn-dead-code",          // Warn when dead code is identified.
    "-Ywarn-extra-implicit",     // Warn when more than one implicit parameter section is defined.
    "-Ywarn-numeric-widen",      // Warn when numerics are widened.
    "-Ywarn-unused:implicits",   // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports",     // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals",      // Warn if a local definition is unused.
    "-Ywarn-unused:params",      // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",     // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates",    // Warn if a private member is unused.
    "-Ywarn-value-discard",      // Warn when non-Unit expression results are unused.
    "-Ybackend-parallelism",
    "4",                                         // Enable paralellisation — change to desired number!
    "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
    "-Ycache-macro-class-loader:last-modified"   // and macro definitions. This can lead to performance improvements.
  )

  lazy val scalacOptions213 = scalacOptionsForAllVersions ++
    Seq(
      "-Xsource:2.13",       // Treat compiler input as Scala source for scala-2.13
      "-Ymacro-annotations", // enable macro annotations
      "-Xlint:-unused,_"     // suppress unused warnings in 2.13
      // "-Xlint"               // enable handy linter warnings
    )

  lazy val scalacOptions212 = scalacOptionsForAllVersions ++
    Seq(
      "-Xsource:2.12",         // Treat compiler input as Scala source for scala-2.12
      "-Yno-adapted-args",     // (only 2.12) Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver
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
