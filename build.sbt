name := """cubefriendly-studio"""

organization := "org.cubefriendly"

version := "0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.m2/repository" ,
  "Snapshot cubefriendly" at "http://cubefriendly-maven.s3.amazonaws.com/snapshot",
  "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/")

libraryDependencies ++= Seq(
  "org.cubefriendly" %% "cubefriendly-core" % "0.1-SNAPSHOT"
)
