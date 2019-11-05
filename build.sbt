import Dependencies._
import ScalacOptions._

val projectName        = "playground-zio"
val projectDescription = "ZIO Playground Project"
val projectVersion     = "0.1.0"

val scala212               = "2.12.10"
val scala213               = "2.13.1"
val supportedScalaVersions = List(scala212, scala213)

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
    initialCommands :=
      s"""|
          |import scala.util.chaining._
          |import zio._
          |import zio.blocking._
          |println
          |""".stripMargin // initialize REPL
  )
)

lazy val root = (project in file("."))
  .aggregate(ziodev)
  .settings(
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
      zioMacrosCore,
      catsCore,
      commonsIO,
      zioTest    % Test,
      zioTestSbt % Test
    ) ++ {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, minor)) if minor >= 13 => Seq.empty
        // Macro paradise not needed in 2.13. Just use scalacOption -Ymacro-annotations. See project/ScalacOptions.scala
        case _ =>
          Seq(compilerPlugin("org.scalamacros" %% "paradise" % "2.1.1" cross CrossVersion.full))
      }
    },
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )

lazy val zioworkshop = (project in file("zioworkshop"))
  .dependsOn(compat213, util)
  .settings(
    name := "zioworkshop",
    description := "Workshop exercises/solutions from https://github.com/jdegoes/zio-intro-game",
    scalacOptions ++= scalacOptionsFor(scalaVersion.value),
    libraryDependencies ++= Seq(
      zio,
      zioStreams,
      zioTest    % Test,
      zioTestSbt % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
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
