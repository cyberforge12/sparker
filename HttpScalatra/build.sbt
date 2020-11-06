name := "HttpScalatra"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % "2.5.4",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.12.v20180830",
  "net.liftweb" % "lift-webkit_2.11" % "3.1.0",
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41"
)