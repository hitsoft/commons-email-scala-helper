organization := "com.hitsoft"

name := "commons-email-scala-helper"

version := "0.1.0"

scalaVersion := "2.11.4"

scalacOptions in Test ++= Seq("-Yrangepos")

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.3.13" % "test",
  "org.apache.commons" % "commons-email" % "1.3.3"
)
