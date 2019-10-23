import sbt._

object Dependencies {

  val catsVersion = "2.0.0"
  val zioVersion  = "1.0.0-RC15"

  val catsCore   = "org.typelevel"  %% "cats-core"   % catsVersion
  val zio        = "dev.zio"        %% "zio"         % zioVersion
  val zioStreams = "dev.zio"        %% "zio-streams" % zioVersion
  val scalaTest  = "org.scalatest"  %% "scalatest"   % "3.0.8"
  val scalaCheck = "org.scalacheck" %% "scalacheck"  % "1.14.2"
  val commonsIO  = "commons-io"     % "commons-io"   % "2.6"
}
