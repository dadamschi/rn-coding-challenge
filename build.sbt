name := "rn-dadamschi-coding-challenge"
version := "0.1"
scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalaj" %% "scalaj-http" % "2.4.0",
  "com.typesafe.play" %% "play" % "2.6.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
)