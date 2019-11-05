import sbt._

object Dependencies {

  val catsVersion = "2.0.0"
  val zioVersion  = "1.0.0-RC16"

  val catsCore      = "org.typelevel"  %% "cats-core"       % catsVersion
  val zio           = "dev.zio"        %% "zio"             % zioVersion
  val zioStreams    = "dev.zio"        %% "zio-streams"     % zioVersion
  val zioTest       = "dev.zio"        %% "zio-test"        % zioVersion
  val zioTestSbt    = "dev.zio"        %% "zio-test-sbt"    % zioVersion
  val zioMacrosCore = "dev.zio"        %% "zio-macros-core" % "0.5.0"
  val scalaTest     = "org.scalatest"  %% "scalatest"       % "3.0.8"
  val scalaCheck    = "org.scalacheck" %% "scalacheck"      % "1.14.2"
  val commonsIO     = "commons-io"     % "commons-io"       % "2.6"
}
