package net.ladstatt.adash

import org.scalajs.dom
import org.scalajs.dom.document

object ArtifactDashboard {

  def main(args: Array[String]): Unit = {
    appendPar(document.body, "Paragraph filled via Scala.js")
  }

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    parNode.textContent = text
    targetNode.appendChild(parNode)
  }
}
