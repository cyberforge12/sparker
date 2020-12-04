import sbt.Keys.libraryDependencies
import Dependencies._
import sbtassembly.AssemblyPlugin.autoImport.{MergeStrategy, PathList}

import scala.Seq

ThisBuild / organization := "com.target"

ThisBuild / version      := "1.0.0"

ThisBuild / scalaVersion := "2.11.12"

cancelable in Global := true

//logLevel in assembly := Level.Debug

autoCompilerPlugins := true

val sparkVersion = "2.4.0"

val liftVersion = "3.3.0"

val log4jVersion = "2.13.3"

val circeVersion = "0.11.2"

lazy val rootSettings = Seq(
  publishArtifact := false,
  publishArtifact in Test := false
)

lazy val commonSettings = Seq(

  // We use a common directory for all of the artifacts
  assemblyOutputPath in assembly := file(name.value + "-" + version.value + ".jar"),

  // This is really a one-time, global setting if all projects
  // use the same folder, but should be here if modified for
  // per-project paths.
//  cleanFiles <+= baseDirectory { base => base / "assembly" },

  test in assembly := {},

  assemblyMergeStrategy in assembly := {
    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
    case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
    case PathList("javax", "ws", xs @ _*) => MergeStrategy.last
    case PathList("org", "aopalliance", xs @ _*) => MergeStrategy.first
    case PathList("org", "apache", "commons", "collections", xs @ _*) => MergeStrategy.last
    case PathList("org", "apache", "commons", "beanutils", xs @ _*) => MergeStrategy.last
    case PathList("org", "apache", "commons", "logging", xs @ _*) => MergeStrategy.last
    case PathList("com", "sun", "research", xs @ _*) => MergeStrategy.last
    case PathList("jersey", "repackaged", xs @ _*) => MergeStrategy.last
    case PathList("org", "apache", "hadoop", "yarn", "factories", "package-info.class") => MergeStrategy.discard
    case PathList("org", "apache", "hadoop", "yarn", "providers", "package-info.class")         => MergeStrategy.discard
    case PathList("org", "apache", "hadoop", "yarn", "factory", "providers", "package-info.class") => MergeStrategy.discard
    case PathList("org", "apache", "hadoop", "yarn", "util", "package-info.class")         => MergeStrategy.discard
    case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class")         => MergeStrategy.first
    case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
    case "META-INF/mailcap" => MergeStrategy.last
    case "META-INF/mimetypes.default" => MergeStrategy.last
    case PathList("META-INF", "MANIFEST>MF") => MergeStrategy.concat
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "plugin.properties" => MergeStrategy.last
    case "git.properties" => MergeStrategy.last
    case "log4j.properties" => MergeStrategy.last
    case "BUILD" => MergeStrategy.discard
    case "logback.xml" => MergeStrategy.first
    case "default" => MergeStrategy.last
    case "rootdoc.txt"     => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },

  assemblyShadeRules in assembly := Seq(
    ShadeRule.rename("org.apache.commons.collections.**" -> "shadedstuff.collections.@1")
      .inLibrary("commons-collections" % "commons-collections" % "3.2.2"),
  ),
  dependencyOverrides ++= {
    Seq(
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.7.1",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.6.7"
    )
  },
)

lazy val rootProject = project.in(file("."))
  .settings(rootSettings: _*)
  .aggregate(http, loader,saver, util)
  .settings(
    name := "sparker",
  )

lazy val util = (project in file("util"))
  .settings(commonSettings: _*)
  .settings(
    name := "util",
    assemblyJarName in assembly := "utils.jar",
    libraryDependencies ++= Seq(
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-web" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion,
      "com.github.scopt" % "scopt_2.11" % "3.7.1" withSources()
    )
  )

lazy val loader = project.in(file("loader"))
  .settings(commonSettings: _*)
  .dependsOn(util)
  .settings(
    mainClass in assembly := Some("com.target.loader.Loader"),
    mainClass in Compile := Some("com.target.loader.Loader"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    name := "loader",
    assemblyJarName in assembly := "loader.jar",
    libraryDependencies ++= Seq(
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      ("org.apache.spark" %% "spark-core" % sparkVersion)
        .exclude("commons-beanutils", "commons-beanutils"),
//        exclude("commons-collections", "commons-collections").
//        exclude("org.mortbay.jetty", "servlet-api").
//        exclude("commons-logging", "commons-logging").
//        exclude("com.esotericsoftware.minlog", "minlog"),
      "org.apache.spark" %% "spark-sql" % sparkVersion,
      "junit" % "junit" % "4.13.1",
      "io.circe" %% "circe-core" % circeVersion withSources(),
      "io.circe" %% "circe-generic" % circeVersion withSources(),
      "io.circe" %% "circe-parser" % circeVersion withSources(),
      "io.circe" %% "circe-yaml" % "0.11.0-M1" withSources(),
      "io.circe" %% "circe-generic-extras" % circeVersion,
      "org.yaml" % "snakeyaml" % "latest.integration",
      "com.lihaoyi" %% "scalatags" % "0.6.7",
      "com.sun.mail" % "javax.mail" % "1.6.2",
      "org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full,
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-web" % log4jVersion,
      //required to avoid dependency conflicts. See: https://stackoverflow.com/questions/17265002/hadoop-no-filesystem-for-scheme-file/27532248#27532248
      "org.apache.hadoop" % "hadoop-hdfs" % "3.2.1",
      "io.github.hakky54" % "sslcontext-kickstart" % "3.0.9"
    //      "org.apache.logging.log4j" % "log4j-to-slf4j" % log4jVersion,
    )
  )

lazy val http = (project in file("HttpScalatra"))
  .settings(commonSettings: _*)
  .dependsOn(util)
  .settings(
    name := "http",
    mainClass in assembly := Some("com.target.http.Main"),
    mainClass in Compile := Some("com.target.http.Main"),
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
  .dependsOn(util)
  .settings(
    name := "saver",
    mainClass in assembly := Some("com.target.saver.Saver"),
    mainClass in Compile := Some("com.target.saver.Saver"),
    assemblyJarName in assembly := "saver.jar",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion withSources(),
      "org.apache.spark" %% "spark-avro" % "2.4.7",
      "org.apache.avro" % "avro" % "1.10.0",
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-web" % log4jVersion,
    ),
    dependencyOverrides ++= {
      Seq(
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.7.1",
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7",
        "com.fasterxml.jackson.core" % "jackson-core" % "2.6.7"
      )
    }
  )
