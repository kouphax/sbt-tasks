sbtPlugin := true

name := "sbt-tasks"

organization := "se.yobriefca"

version := "0.1.0"

sbtVersion := "0.13.0"

scalaVersion := "2.10.3"

publishTo := Some(Resolver.file("yobriefca.se-repo", Path.userHome / "Projects" / "yobriefca.se" / "source" / "maven" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))