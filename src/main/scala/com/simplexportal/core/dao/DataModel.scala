package com.simplexportal.core.dao

object DataModel {

  sealed trait Metadata

  case class PageMetadata(path: String, template: String, encoding: String = "UTF-8") extends Metadata

  case class ComponentMetadata(path: String, `type`: String, name: String, orderExecution: Int, transformers: Seq[String], parameters: Map[String, String]) extends Metadata

  case class TemplateMetadata(path: String, encoding: String, mimeType: String) extends Metadata

  case class ResourceMetadata(path: String, encoding: String, mimeType: String) extends Metadata

  case class FolderMetadata(path: String, defaultContent: Option[String], listContents: Boolean) extends Metadata

}



