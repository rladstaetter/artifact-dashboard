enablePlugins(ScalaJSPlugin)

name := "artifact-dashboard"
scalaVersion := "2.12.10" // or any other Scala version >= 2.11.12

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0"

