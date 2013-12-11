import bintray.Keys._

sbtPlugin := true

name := "sbt-tasks"

description := "sbt-tasks plugin akin to the rails/rake custom tasks"

organization := "se.yobriefca"

version := "0.3.16"

publishMavenStyle := false

sbtVersion := "0.13.0"

scalaVersion := "2.10.3"

bintrayPublishSettings

repository in bintray := "sbt-plugins"

bintrayOrganization in bintray := None

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

packageLabels in bintray := Seq("scala", "sbt")
