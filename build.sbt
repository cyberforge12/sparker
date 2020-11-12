name := "com/target/loader"
organization := "com.target"

autoCompilerPlugins := true

version := "0.1"

scalaVersion := "2.11.12"

val sparkVersion = "2.4.0"

val circeVersion = "0.11.2"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "junit" % "junit" % "4.13.1" % Test,
  "io.circe" %% "circe-yaml" % "0.9.0" withSources(),
  "io.circe" %% "circe-core" % circeVersion withSources(),
  "io.circe" %% "circe-generic" % circeVersion withSources(),
  "io.circe" %% "circe-parser" % circeVersion withSources(),
  "io.circe" %% "circe-generic-extras" % "0.12.0-M3",
  "org.yaml" % "snakeyaml" % "1.27",
  "org.apache.logging.log4j" % "log4j-api" % "2.13.3",
  "org.apache.logging.log4j" % "log4j-core" % "2.13.3",
  "com.lihaoyi" %% "scalatags" % "0.6.7",
  "com.sun.mail" % "javax.mail" % "1.6.2",
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full,
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
