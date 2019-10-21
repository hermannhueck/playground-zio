val projectName        = "playground-zio"
val projectDescription = "ZIO Playground Project"
val projectVersion     = "0.1.0"

val scala212               = "2.12.10"
val scala213               = "2.13.1"
val supportedScalaVersions = List(scala212, scala213)

val zio              = "dev.zio"                %% "zio"                     % "1.0.0-RC15"
val scalaTest        = "org.scalatest"          %% "scalatest"               % "3.0.8"
val scalaCheck       = "org.scalacheck"         %% "scalacheck"              % "1.14.2"
val collectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2"

inThisBuild(
  Seq(
    version := projectVersion,
    scalaVersion := scala213,
    crossScalaVersions := supportedScalaVersions,
    publish / skip := true,
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",        // source files are in UTF-8
      "-deprecation", // warn about use of deprecated APIs
      "-unchecked",   // warn about unchecked type parameters
      "-feature",     // warn about misused language features
      // "-Xfatal-warnings", // fail the compilation if there are any warnings
      "-explaintypes", // explain type errors in more detail
      "-Xcheckinit"    // wrap field accessors to throw an exception on uninitialized access
    ),
    libraryDependencies ++= Seq(
      collectionCompat,
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
    crossScalaVersions := Seq.empty,
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, minor)) if minor >= 13 =>
          Seq(
            "-Xlint:-unused,_" // suppress unused warnings in 2.13
            // "-Xlint"
          )
        case _ =>
          Seq(
            "-Ypartial-unification", // (removed in scala 2.13) allow the compiler to unify type constructors of different arities
            "-language:higherKinds", // (not required since scala 2.13.1) suppress warnings when using higher kinded types
            "-Xlint"                 // enable handy linter warnings
          )
      }
    }
  )

lazy val ziodev = (project in file("ziodev"))
  .dependsOn(compat213, util)
  .settings(
    name := "ziodev",
    description := "Code samples from the zio website https://zio.dev",
    libraryDependencies ++= Seq(
      zio
    )
  )

lazy val compat213 = (project in file("compat213"))
  .settings(
    name := "compat213",
    description := "compat library providing scala 2.13 extensions for scala 2.12"
  )

lazy val util = (project in file("util"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "util",
    description := "Utilities",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "build"
  )

// https://github.com/typelevel/kind-projector
addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full)
// https://github.com/oleg-py/better-monadic-for
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
