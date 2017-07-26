import Dependencies._

lazy val root = (project in file(".")).enablePlugins(PlayScala).
  settings(
    inThisBuild(List(
      organization := "com.duskeagle",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "FreeGwentServer",
    libraryDependencies += scalaTest % Test
  )

libraryDependencies += guice
