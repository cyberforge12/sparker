name := "loader_sbt"

version := "0.1"

scalaVersion := "2.11.12"

val sparkVersion = "2.4.0"

val circeVersion = "0.11.2"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "junit" % "junit" % "4.13.1" % Test,
  "io.circe" %% "circe-yaml" % "0.9.0" withSources(),
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "org.yaml" % "snakeyaml" % "1.27",
  "org.apache.logging.log4j" % "log4j-api" % "2.13.3",
  "org.apache.logging.log4j" % "log4j-core" % "2.13.3"
)
