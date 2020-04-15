package net.ladstatt.adash

import scala.scalajs.js

/**
 * contains mappings for rest api call such that we can use the parsed javascript objects
 * as scala case classes
 */
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
