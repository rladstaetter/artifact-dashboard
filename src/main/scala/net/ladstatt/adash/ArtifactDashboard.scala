package net.ladstatt.adash

import org.scalajs.dom.document
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.Element

import scala.concurrent.ExecutionContextExecutor
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


object ArtifactDashboard {

  def main(args: Array[String]): Unit = {
    queryNexus()
  }

  def queryNexus(): Unit = {
    // executioncontext needed for ajax call
    implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

    Ajax.get("http://127.0.0.1:8081/service/rest/v1/assets?repository=maven-public") onComplete {
      case Success(v) =>
        val assetsResult = JSON.parse(v.responseText).asInstanceOf[AssetsResult[Asset]]
        for (a <- assetsResult.items) {
          document.body.appendChild(p(a.id))
        }
        org.scalajs.dom.window.alert(assetsResult.continuationToken)
      case Failure(e) =>
        e.printStackTrace()
        org.scalajs.dom.window.alert(e.getMessage)
    }
  }

  def p(text: String): Element = {
    val p = document.createElement("p")
    p.textContent = text
    p
  }

}
