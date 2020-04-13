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

case class MavenCoords(groupId: String
                       , artifactId: String
                       , baseVersion: String
                       , extension: String
                       , classifier: String)

object ArtifactDashboard {

  // executioncontext needed for ajax call
  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  // url of used nexus
  val nexusBaseUrl: String = "http://127.0.0.1:8081/"

  // rest url which we'll use to retrieve information about assets
  val assetsSearchUri: String = "service/rest/v1/search/assets?"

  private val artifact: MavenCoords = MavenCoords("net.ladstatt", "fx-animations", "1.0-SNAPSHOT", "jar", "jar-with-dependencies")

  def main(args: Array[String]): Unit = {
    queryNexus(artifact) onComplete {
      case Failure(e) => org.scalajs.dom.window.alert(e.getMessage)
      case Success(either) => either match {
        case Left(e) => org.scalajs.dom.window.alert(e.getMessage)
        case Right(downloadUrl) =>
          document.body.appendChild(a(artifact.artifactId, downloadUrl))
      }
    }
  }

  def queryNexus(mvnCoords: MavenCoords): Future[Either[Throwable, String]] = {

    val requestString = nexusBaseUrl + assetsSearchUri +
      Map("repository" -> "testrepo-snapshot"
        , "sort" -> "version"
        , "direction" -> "desc"
        , "maven.groupId" -> mvnCoords.groupId
        , "maven.artifactId" -> mvnCoords.artifactId
        , "maven.baseVersion" -> mvnCoords.baseVersion
        , "maven.extension" -> mvnCoords.extension
        , "maven.classifier" -> mvnCoords.classifier).map {
        case (k, v) => s"$k=$v"
      }.mkString("&")

    Ajax.get(requestString).map {
      xml =>
        val assetsResult = JSON.parse(xml.responseText).asInstanceOf[AssetsResult[Asset]]
        assetsResult.items.headOption match {
          case None => Left(new RuntimeException("No artifact with given coordinates found: " + mvnCoords))
          case Some(x) => Right(x.downloadUrl)
        }
    }

  }

  def a(text: String, href: String): Element = {
    val a = document.createElement("a")
    a.textContent = text
    a.setAttribute("href", href)
    a
  }

}
