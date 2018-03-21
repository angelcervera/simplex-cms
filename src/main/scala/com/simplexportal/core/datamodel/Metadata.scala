package com.simplexportal.core.datamodel

import java.nio.file.Path

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write, writePretty}
import better.files._
import better.files.Dsl.SymbolicOperations

object Metadata {

  implicit val formats = Serialization.formats(NoTypeHints)

  implicit class JSonUtilities(obj: Metadata) {
    def toJson: String = writePretty(obj)
    def toJson(file: File): File = {
      file.parent.createDirectories()
      file < toJson
    }
  }

  def fromJson(file: File) = ???

  sealed trait Metadata

  case class PageMetadata(path: String, template: String) extends Metadata

  case class ComponentMetadata(`type`: String, name: String, orderExecution: Int, parameters: Map[String, String]) extends Metadata

  case class TemplateMetadata(path: String, encoding: String, mimeType: String) extends Metadata

  case class ResourceMetadata(path: String, encoding: String, mimeType: String) extends Metadata

}



