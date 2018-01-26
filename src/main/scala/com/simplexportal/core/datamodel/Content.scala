package com.simplexportal.core.datamodel


/**
  * The site is represented as a tree of contents.
  * The path is the id of the content in the tree and except for the root, can not finish on '/' (except the root) or "."
  *
  * @param path Full path to the content.
  */
sealed abstract class Content(path: String) {

  /**
   * Calculate the name of the content from the path.
   */
  def name: String =path.split("/") match {
    case Array() => "/"
    case parts => parts.last
  }

  /**
   * Calculate the path to the parent from the path.
   *
   * @return None if it is the Root.
   */
  def parent: Option[String] = path match {
    case "/" => None
    case _ => Some("/" + path.tail.split("/").dropRight(1).mkString("/"))
  }

  require( path == "/" || !Seq('.', '/').contains(path.last), s"Trying to create a content with invalid path [${path}]")
}

case class Folder(path: String, default: Option[String]) extends Content(path)

case class Resource(path: String, mimeType: String, encoding: Option[String] = None) extends Content(path)

case class Page(path: String, mimeType: String = "text/html", encoding: String = "UTF-8") extends Content(path)

case class Component(
  id: String,
  `type`: String,
  fullTag: String,
  bodyTag: String,
  processedTag: String,
  executionOrder: Long
) extends Content(id: String)
