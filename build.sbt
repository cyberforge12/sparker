import sbt.Keys.libraryDependencies
import Dependencies._
import sbtassembly.AssemblyPlugin.autoImport.{MergeStrategy, PathList}

import scala.Seq
ThisBuild / organization := "com.target"

ThisBuild / version      := "1.0.0"

ThisBuild / scalaVersion := "2.11.12"

autoCompilerPlugins := true

val sparkVersion = "2.4.0"

val liftVersion = "3.3.0"

val log4jVersion = "latest.integration"

lazy val rootSettings = Seq(
  publishArtifact := false,
  publishArtifact in Test := false
)

lazy val commonSettings = Seq(

  // We use a common directory for all of the artifacts
  assemblyOutputPath in assembly := baseDirectory.value /
    "assembly" / (name.value + "-" + version.value + ".jar"),

  // This is really a one-time, global setting if all projects
  // use the same folder, but should be here if modified for
  // per-project paths.
//  cleanFiles <+= baseDirectory { base => base / "assembly" },

  test in assembly := {},

  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case PathList("org", "apache", "hadoop", "yarn", "factories", "package-info.class")         => MergeStrategy.discard
    case PathList("org", "apache", "hadoop", "yarn", "providers", "package-info.class")         => MergeStrategy.discard
    case PathList("org", "apache", "hadoop", "yarn", "util", "package-info.class")         => MergeStrategy.discard
    case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class")         => MergeStrategy.first
    case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
    case "META-INF/mailcap" => MergeStrategy.last
    case "META-INF/mimetypes.default" => MergeStrategy.last
    case "plugin.properties" => MergeStrategy.last
    case "log4j.properties" => MergeStrategy.last
    case "BUILD" => MergeStrategy.discard
    case "logback.xml" => MergeStrategy.first
    case "default" => MergeStrategy.last
    case "rootdoc.txt"     => MergeStrategy.discard
    case other: Any => MergeStrategy.defaultMergeStrategy(other)
  },

  //example of excluded jars
  assemblyExcludedJars in assembly := {
    val cp = (fullClasspath in assembly).value
    cp filter {
      _.data.getName.matches(".*finatra-scalap-compiler-deps.*")
    }
  },
)

lazy val rootProject = project.in(file("."))
  .settings(rootSettings: _*)
  .aggregate(http, loader,saver)
  .settings(
    name := "sparker",
  )

lazy val loader = project.in(file("loader"))
  .settings(commonSettings: _*)
  .settings(
    mainClass in Compile := Some("com.target.loader.Loader"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    name := "loader",
    assemblyJarName in assembly := "loader.jar",
    libraryDependencies ++= Seq(
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,
      "junit" % "junit" % "4.13.1",
      "io.circe" %% "circe-core" % "latest.integration" withSources(),
      "io.circe" %% "circe-generic" % "latest.integration" withSources(),
      "io.circe" %% "circe-parser" % "latest.integration" withSources(),
      "io.circe" %% "circe-yaml" % "latest.integration" withSources(),
      "io.circe" %% "circe-generic-extras" % "latest.integration",
      "org.yaml" % "snakeyaml" % "latest.integration",
      "com.lihaoyi" %% "scalatags" % "0.6.7",
      "com.sun.mail" % "javax.mail" % "1.6.2",
      "org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full,
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-web" % log4jVersion,
//      "org.apache.logging.log4j" % "log4j-to-slf4j" % log4jVersion,
    )
  )

lazy val http = (project in file("HttpScalatra"))
  .settings(commonSettings: _*)
  .settings(
    name := "http",
    mainClass in Compile := Some("Main"),
    assemblyJarName in assembly := "http.jar",
    libraryDependencies ++= Seq(
      "org.scalatra" %% "scalatra" % "2.5.4",
      "org.eclipse.jetty" % "jetty-webapp" % "9.4.12.v20180830",
      "net.liftweb" % "lift-webkit_2.11" % "3.1.0",
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-web" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion,
//      "org.apache.logging.log4j" % "log4j-to-slf4j" % log4jVersion,
    )
  )

lazy val saver = (project in file("saver"))
  .settings(commonSettings: _*)
  .settings(
    name := "saver",
    mainClass in Compile := Some("com.target.loader.Saver"),
    assemblyJarName in assembly := "saver.jar",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion withSources(),
      "io.circe" %% "circe-core" % "latest.integration" withSources(),
      "io.circe" %% "circe-generic" % "latest.integration" withSources(),
      "io.circe" %% "circe-parser" % "latest.integration" withSources(),
      "org.apache.spark" %% "spark-avro" % "2.4.7",
      "org.apache.avro" % "avro" % "1.10.0",
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-web" % log4jVersion,
//      "org.apache.logging.log4j" % "log4j-to-slf4j" % log4jVersion,
),
    dependencyOverrides ++= {
      Seq(
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.7.1",
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7",
        "com.fasterxml.jackson.core" % "jackson-core" % "2.6.7"
      )
    }
  )
