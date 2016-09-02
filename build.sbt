version := "0.1-SNAPSHOT"

name := "try-scalajs-crossproject"

val app = crossProject.settings(
  unmanagedSourceDirectories in Compile +=
    baseDirectory.value / "shared" / "main" / "scala",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "scalatags" % "0.4.6",
    "com.lihaoyi" %%% "upickle" % "0.2.7",
    "com.lihaoyi" %%% "autowire" % "0.2.5"
  ),
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq(
    "-deprecation"
  )
).jsSettings(
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.8.0"
).jvmSettings(
  // JVM-specific settings here
  libraryDependencies ++= Seq(
    "io.spray" %% "spray-can" % "1.3.2",
    "io.spray" %% "spray-routing" % "1.3.2",
    "com.typesafe.akka" %% "akka-actor" % "2.3.6",
    "plus.coding" %% "ckrecom" % "1.0-SNAPSHOT"
  )
)

lazy val appJS = app.js.settings(
)

lazy val appJVM = app.jvm.settings(
  (resources in Compile) += (fastOptJS in (appJS, Compile)).value.data
)

