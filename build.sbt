enablePlugins(ScalaJSPlugin)

name := "artifact-dashboard"
scalaVersion := "2.12.10"

libraryDependencies ++=
  Seq("org.scala-js" %%% "scalajs-dom" % "1.0.0"
    , "org.webjars" % "jquery" % "3.5.0"
    , "org.webjars" % "bootstrap" % "4.4.1")

// defines sbt name filters for unpacking
val bootstrapMinJs: NameFilter = "**/bootstrap.bundle.js"
val bootstrapMinCss: NameFilter = "**/bootstrap.css"
val bootstrapFilters: NameFilter = bootstrapMinCss | bootstrapMinJs
val jqueryJs: NameFilter = "**/jquery.js"
val allFilter: NameFilter = "**/**"

// magic to invoke unpacking stuff in the compile phase
resourceGenerators in Compile ++= Seq(Def.task {
  val to = (target in Compile).value

  val jqueryJar = (update in Compile).value
    .select(configurationFilter("compile"))
    .filter(_.name.contains("jquery"))
    .head
  unpackjar(jqueryJar, to, jqueryJs)

  val bootstrapJar = (update in Compile).value
    .select(configurationFilter("compile"))
    .filter(_.name.contains("bootstrap"))
    .head
  unpackjar(bootstrapJar, to, bootstrapFilters)

  Seq.empty[File]
}.taskValue)

// a helper function which unzips files defined in given namefilter
// to a given directory, along with some reporting
def unpackjar(jar: File, to: File, filter: NameFilter): File = {
  val files: Set[File] = IO.unzip(jar, to, filter)
  // print it out so we can see some progress on the sbt console
  println(s"Processing $jar and unzipping to $to")
  files foreach println
  jar
}
