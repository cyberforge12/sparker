import sbt.Keys.libraryDependencies
organization := "com.target"
autoCompilerPlugins := true

ThisBuild / scalaVersion := "2.11.12"

val circeVersion = "0.11.2"
val sparkVersion = "2.4.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "sparker",
    version := "0.3.0"
  )

lazy val loader = (project in file("loader"))
  .settings(
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    name := "loader",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,
      "junit" % "junit" % "4.13.1" % Test,
      "io.circe" %% "circe-yaml" % "0.9.0" withSources(),
      "io.circe" %% "circe-core" % circeVersion withSources(),
      "io.circe" %% "circe-generic" % circeVersion withSources(),
      "io.circe" %% "circe-parser" % circeVersion withSources(),
      "io.circe" %% "circe-generic-extras" % "0.12.0-M3",
      "org.apache.logging.log4j" % "log4j-api" % "2.13.3",
      "org.apache.logging.log4j" % "log4j-core" % "2.13.3",
      "org.yaml" % "snakeyaml" % "1.27",
      "com.lihaoyi" %% "scalatags" % "0.6.7",
      "com.sun.mail" % "javax.mail" % "1.6.2",
      "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full,
    )
  )

lazy val http = (project in file("HttpScalatra"))
  .settings(
    name := "http",
    libraryDependencies ++= Seq(
      "org.scalatra" %% "scalatra" % "2.5.4",
      "org.eclipse.jetty" % "jetty-webapp" % "9.4.12.v20180830",
      "net.liftweb" % "lift-webkit_2.11" % "3.1.0",
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
      "org.apache.logging.log4j" % "log4j-api" % "2.13.3",
      "org.apache.logging.log4j" % "log4j-core" % "2.13.3",
      "org.apache.logging.log4j" % "log4j-web" % "2.13.3",
    )
  )

lazy val daver = (project in file("saver"))
  .settings(
    name := "saver",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
      "org.apache.logging.log4j" % "log4j-api" % "2.13.3",
      "org.apache.logging.log4j" % "log4j-core" % "2.13.3",
      "org.apache.logging.log4j" % "log4j-web" % "2.13.3",
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,
      "io.circe" %% "circe-core" % circeVersion withSources(),
      "io.circe" %% "circe-generic" % circeVersion withSources(),
      "io.circe" %% "circe-parser" % circeVersion withSources(),
      "org.apache.spark" %% "spark-avro" % "2.4.7",
      "org.apache.avro" % "avro" % "1.10.0",
    )
  )

