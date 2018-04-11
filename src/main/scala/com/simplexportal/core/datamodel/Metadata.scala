package com.simplexportal.core.datamodel

object Metadata {

  sealed trait Metadata

  case class PageMetadata(path: String, template: String, encoding: String = "UTF-8") extends Metadata

  case class ComponentMetadata(`type`: String, name: String, orderExecution: Int, parameters: Map[String, String]) extends Metadata

  case class TemplateMetadata(path: String, encoding: String, mimeType: String) extends Metadata

  case class ResourceMetadata(path: String, encoding: String, mimeType: String) extends Metadata

  case class FolderMetadata(path: String, defaultContent: Option[String], listContents: Boolean) extends Metadata

}



