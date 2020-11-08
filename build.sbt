name := "loader_sbt"

version := "0.1"

scalaVersion := "2.12.12"

val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.0.1",
  "org.apache.spark" %% "spark-sql" % "3.0.1",
  "junit" % "junit" % "4.13.1" % Test,
  "io.circe" %% "circe-yaml" % "0.9.0" withSources(),
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "org.yaml" % "snakeyaml" % "1.27"
)
