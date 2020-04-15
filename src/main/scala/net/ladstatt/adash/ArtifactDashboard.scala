package net.ladstatt.adash

import org.scalajs.dom.document
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.{Element, HTMLElement}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.{Failure, Success}


@JSExportTopLevel("ArtifactDashboard")
object ArtifactDashboard {

  // executioncontext needed for ajax call
  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  // url of used nexus
  val nexusBaseUrl: String = "http://127.0.0.1:8081/"

  // rest url which we'll use to retrieve information about assets
  val assetsSearchUri: String = "service/rest/v1/search/assets?"

  private val artifact: MavenCoords = MavenCoords("testrepo-snapshot", "net.ladstatt", "fx-animations", "1.0-SNAPSHOT", Option("jar"), Option("jar-with-dependencies"))

  // some random artifact just to show the principle
  private val artifact2: MavenCoords = MavenCoords("maven-public", "net.java.dev.jna", "jna", "4.1.0")

  val artifacts = Seq(artifact, artifact2)

  /**
   * can be called via javascript in the html page
   */
  @JSExport
  def renderArtifactList(htmlElement: Div): Unit = {
    Future.sequence(artifacts.map(queryNexus)) onComplete {
      case Failure(e) => org.scalajs.dom.window.alert(e.getMessage)
      case Success(assetsOrFailure) =>
        var errors = ""
        assetsOrFailure foreach {
          case Left(e) => errors = errors + e.getMessage + "\n"
          case Right(nexusAsset) =>
            htmlElement.appendChild(a(nexusAsset.mavenCoords.asString, nexusAsset.downloadUrl))
            htmlElement.appendChild(br)
        }
        if (errors.nonEmpty) {
          org.scalajs.dom.window.alert("Error(s): \n\n" + errors)
        }
    }
  }

  def queryNexus(mvnCoords: MavenCoords): Future[Either[Throwable, NexusAsset]] = {

    val requestString = nexusBaseUrl + assetsSearchUri +
      (Map("repository" -> mvnCoords.repo
        , "sort" -> "version"
        , "direction" -> "desc"
        , "maven.groupId" -> mvnCoords.groupId
        , "maven.artifactId" -> mvnCoords.artifactId
        , "maven.baseVersion" -> mvnCoords.baseVersion) ++
        (if (mvnCoords.extension.isDefined) Map("maven.extension" -> mvnCoords.extension.getOrElse("")) else Map()) ++
        (if (mvnCoords.classifier.isDefined) Map("maven.classifier" -> mvnCoords.classifier.getOrElse("")) else Map())
        ).map {
        case (k, v) => s"$k=$v"
      }.mkString("&")

    // org.scalajs.dom.window.alert(requestString)
    Ajax.get(requestString).map {
      xml =>
        val assetsResult = JSON.parse(xml.responseText).asInstanceOf[AssetsResult[Asset]]
        assetsResult.items.headOption match {
          case None => Left(new RuntimeException("No artifact with given coordinates found: " + mvnCoords))
          case Some(x) => Right(NexusAsset(mvnCoords, x.downloadUrl))
        }
    }

  }

  def br: Element = document.createElement("br")

  def a(text: String, href: String): Element = {
    val a = document.createElement("a")
    a.textContent = text
    a.setAttribute("href", href)
    a
  }

}
