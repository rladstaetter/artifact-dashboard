# Artifact Dashboard

A project to query a Nexus Repository manager via it's Rest API. 

Can be configured to specific group id's / artifactid's and will list those in a nicely formatted way.

Uses Scala.js, sbt and webjars in order to download some javascript dependencies.

There is also a blog post about this project which describes the creation of this project in a tutorial.

This project helps to search for certain artifacts on a nexus. It provides a configurable 'summary' 
page for certain artifacts - handy if you have multiple snapshots and are interested only in the last published one.

## Building

This project uses Scala and sbt, it should run out of the box if you have sbt installed in a recent version 
(see https://www.scala-sbt.org/download.html) on how to setup sbt for your OS.

As soon as you have sbt running, go to the base directory of this project and issue

    sbt
    
After some minutes it should complete and a prompt awaits your command. Either enter

    fastOptJS
    
for compiling or

    fullOptJS
    
if you want the full optimized version. After that you can open 'artifact-dashboard.html' with a browser.

Attention: If you run into an issue related to CORS you have to make sure that it is served by the same webserver like the called
rest service.

## License

Apache License 2.0



