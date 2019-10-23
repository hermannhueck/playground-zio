val projectName        = "playground-zio"
val projectDescription = "ZIO Playground Project"
val projectVersion     = "0.1.0"

val scala212               = "2.12.10"
val scala213               = "2.13.1"
val supportedScalaVersions = List(scala212, scala213)

val catsVersion = "2.0.0"
val zioVersion  = "1.0.0-RC15"

val catsCore   = "org.typelevel"  %% "cats-core"   % catsVersion
val zio        = "dev.zio"        %% "zio"         % zioVersion
val zioStreams = "dev.zio"        %% "zio-streams" % zioVersion
val scalaTest  = "org.scalatest"  %% "scalatest"   % "3.0.8"
val scalaCheck = "org.scalacheck" %% "scalacheck"  % "1.14.2"
val commonsIO  = "commons-io"     % "commons-io"   % "2.6"

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

inThisBuild(
  Seq(
    version := projectVersion,
    scalaVersion := scala213,
    crossScalaVersions := supportedScalaVersions,
    publish / skip := true,
    libraryDependencies ++= Seq(
      scalaTest  % Test,
      scalaCheck % Test
    ),
    initialCommands := s"""
      import scala.util.chaining._
      import zio._
      import zio.blocking._
      println
      """.stripMargin // initialize REPL
  )
)

lazy val root = (project in file("."))
  .aggregate(ziodev)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "build",
    name := projectName,
    description := projectDescription,
    crossScalaVersions := Seq.empty
  )

lazy val ziodev = (project in file("ziodev"))
  .dependsOn(compat213, util)
  .settings(
    name := "ziodev",
    description := "Code samples from the zio website https://zio.dev",
    scalacOptions ++= scalacOptionsFor(scalaVersion.value),
    libraryDependencies ++= Seq(
      zio,
      zioStreams,
      catsCore,
      commonsIO
    )
  )

lazy val compat213 = (project in file("compat213"))
  .settings(
    name := "compat213",
    description := "compat library providing scala 2.13 extensions for scala 2.12",
    scalacOptions ++= scalacOptionsFor(scalaVersion.value)
  )

lazy val util = (project in file("util"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "util",
    description := "Utilities",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "build",
    scalacOptions ++= scalacOptionsFor(scalaVersion.value)
  )

// https://github.com/typelevel/kind-projector
addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full)
// https://github.com/oleg-py/better-monadic-for
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
