sbtPlugin := true

name := "sbt-tasks"

organization := "se.yobriefca"

version := "0.1.2"

sbtVersion := "0.13.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test"
)


publishTo := Some(Resolver.file("yobriefca.se-repo", Path.userHome / "Projects" / "yobriefca.se" / "source" / "maven" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))