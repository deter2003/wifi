name := """wifi"""

version := "1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.18"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "org.webjars" %% "webjars-play" % "2.4.0-2",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "jquery" % "2.1.4"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
// routesGenerator := InjectedRoutesGenerator

lazy val myProject = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)


fork in run := false

routesGenerator := InjectedRoutesGenerator