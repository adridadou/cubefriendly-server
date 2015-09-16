enablePlugins(JavaAppPackaging)

name         := "cubefriendly-studio"
organization := "org.cubefriendly"
version      := "0.1-SNAPSHOT"
scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.m2/repository" ,
  "Snapshot cubefriendly" at "http://cubefriendly-maven.s3.amazonaws.com/snapshot",
  "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/")

publishMavenStyle := true

publishTo := {
  val repoType = if (isSnapshot.value) "snapshot" else "release"
  Some(("Cubefriendly " + repoType) at "s3://cubefriendly-maven.s3.amazonaws.com/" + repoType)
}

libraryDependencies ++= {
  val akkaStreamV = "1.0"
  val scalaTestV  = "2.2.4"
  val scaldiV     = "0.5.5"
  Seq(
    "com.typesafe.akka" %% "akka-http-core-experimental"          % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental"               % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaStreamV,
    "org.scaldi"        %% "scaldi-akka"                          % scaldiV,
    "org.scaldi"        %% "scaldi"                               % scaldiV,
    "org.cubefriendly"  %% "cubefriendly-core"                    % version.value,
    "com.typesafe.akka" %% "akka-http-testkit-experimental"       % akkaStreamV % "test",
    "org.scalatest"     %% "scalatest"                            % scalaTestV % "test",
    "org.scalamock"     %% "scalamock-scalatest-support"          % "3.2"      % "test"
  )
}

Revolver.settings
