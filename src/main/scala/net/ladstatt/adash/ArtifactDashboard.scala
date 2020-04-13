package net.ladstatt.adash

import org.scalajs.dom.document
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.Element

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

@js.native
trait CheckSum extends js.Object {
  def md5: String

  def sha1: String
}

@js.native
trait Asset extends js.Object {
  def id: String

  def path: String

  def downloadUrl: String

  def repository: String

  def format: String

  def checksum: CheckSum
}

@js.native
trait AssetsResult[T] extends js.Object {
  def items: js.Array[T]

  def continuationToken: String
}

object MavenCoords {

  // convenience constructor
  def apply(repo: String, groupId: String, artifactId: String, baseVersion: String): MavenCoords = {
    MavenCoords(repo, groupId, artifactId, baseVersion, None, None)
  }

}

case class MavenCoords(repo: String
                       , groupId: String
                       , artifactId: String
                       , baseVersion: String
                       , extension: Option[String]
                       , classifier: Option[String]) {
  val asString = s"$groupId:$artifactId:$baseVersion:${extension.getOrElse("")}:${classifier.getOrElse("")}"
}

case class NexusAsset(mavenCoords: MavenCoords, downloadUrl: String)

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

  def main(args: Array[String]): Unit = {
    Future.sequence(artifacts.map(queryNexus)) onComplete {
      case Failure(e) => org.scalajs.dom.window.alert(e.getMessage)
      case Success(assetsOrFailure) =>
        var errors = ""
        assetsOrFailure foreach {
          case Left(e) => errors = errors + e.getMessage + "\n"
          case Right(nexusAsset) =>
            document.body.appendChild(a(nexusAsset.mavenCoords.asString, nexusAsset.downloadUrl))
            document.body.appendChild(br)
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
