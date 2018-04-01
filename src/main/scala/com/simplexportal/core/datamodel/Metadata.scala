package com.simplexportal.core.datamodel

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.writePretty
import better.files._
import better.files.Dsl.SymbolicOperations

object Metadata {

  implicit val formats = Serialization.formats(NoTypeHints)

  implicit class MetadataJsonUtilities(obj: Metadata) {
    def toJson: String = writePretty(obj)
    def toJson(file: File): File = {
      file.parent.createDirectories()
      file < toJson
    }
  }

  sealed trait Metadata

  case class PageMetadata(path: String, template: String, encoding: String = "UTF-8") extends Metadata

  case class ComponentMetadata(`type`: String, name: String, orderExecution: Int, parameters: Map[String, String]) extends Metadata

  case class TemplateMetadata(path: String, encoding: String, mimeType: String) extends Metadata

  case class ResourceMetadata(path: String, encoding: String, mimeType: String) extends Metadata

}



