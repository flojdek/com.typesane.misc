name := """misc"""

organization := "com.typesane"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.10.30",
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

bintraySettings

com.typesafe.sbt.SbtGit.versionWithGit
