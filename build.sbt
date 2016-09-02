version := "0.1-SNAPSHOT"

name := "ecom-shop-demo"

val app = crossProject.settings(
  unmanagedSourceDirectories in Compile +=
    baseDirectory.value / "shared" / "main" / "scala",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "scalatags" % "0.6.0",
    "com.lihaoyi" %%% "upickle" % "0.4.2",
    "com.lihaoyi" %%% "autowire" % "0.2.5"
  ),
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature"
  )
).jsSettings(
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.8.0"
).jvmSettings(
  // JVM-specific settings here
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.4.9",
    "com.typesafe.akka" %% "akka-http-experimental" % "2.4.9",
    "plus.coding" %% "ckrecom" % "1.0-SNAPSHOT"
  )
)

lazy val appJS = app.js.settings(
)

lazy val appJVM = app.jvm.settings(
  (resources in Compile) += (fastOptJS in (appJS, Compile)).value.data
)

