package net.ladstatt.adash

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