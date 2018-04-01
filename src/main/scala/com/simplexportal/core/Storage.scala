package com.simplexportal.core

import better.files._
import com.simplexportal.core.datamodel.Metadata.{ComponentMetadata, PageMetadata, ResourceMetadata, TemplateMetadata}
import com.typesafe.scalalogging.LazyLogging
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.read

// TODO: Remove data from the case class, so only Page is necessary. Read data under demand.
sealed trait StorageDataModel
case class Component(metadata: ComponentMetadata, data: String) extends StorageDataModel
case class Template(metadata: TemplateMetadata, data: String) extends StorageDataModel
case class Page(metadata: PageMetadata, components: Seq[Component], template: Template) extends StorageDataModel

case class Storage(rootPath: String) extends LazyLogging {

  implicit val formats = DefaultFormats

  implicit class ResourceMetadataEnrich(metadata: ResourceMetadata) {
    def data = ( rootFile / s"data/${metadata.path}" )
  }

  implicit class TemplateMetadataEnrich(metadata: TemplateMetadata) {
    def data = (rootFile / s"templates${metadata.path}/data.html")
  }

  lazy val rootFile = {
    val tmpFile = rootPath.toFile
    require(tmpFile.exists && tmpFile.isDirectory, s"${tmpFile} does not exist or it is not a directory")
    tmpFile
  }

  lazy val templates = readTemplates

  def pages: Seq[Page] =
    (rootFile / "meta")
      .listRecursively
      .filter(_.name.endsWith(".page.json") )
      .map(_.contentAsString) // TODO: What is better? Two map or one?
      .map(read[PageMetadata])
      .map(metadata => Page(metadata, readComponents(metadata), templates.get(metadata.template).get))
      .toSeq

  private def readTemplates: Map[String, Template] =
    (rootFile / "templates")
      .listRecursively
      .filter(_.name == "metadata.json" )
      .map(_.contentAsString)
      .map(read[TemplateMetadata])
      .map(template => template.path -> Template(template, template.data.contentAsString))
      .toMap

  private def readComponents(pageMetadata: PageMetadata): Seq[Component] = {
    val absolutePath = (rootFile / s"meta${pageMetadata.path}")
    val pageName = absolutePath.name
    absolutePath
      .parent
      .list
      .filter(f => f.name.startsWith(pageName) && f.name.endsWith(".component.json"))
      .map(_.contentAsString)
      .map(read[ComponentMetadata])
      .map(cmp => Component(cmp, readComponentData(pageMetadata, cmp).contentAsString))
      .toSeq
  }

  private def readComponentData(page: PageMetadata, cmp: ComponentMetadata): File = {
    val pageFile = page.path.toFile
    (rootFile / s"data${pageFile.parent.toString()}/_simplexportal_page_${pageFile.name}_${cmp.name}.html")
  }

  def resources =
    (rootFile / "meta")
      .listRecursively
      .filter(_.name.endsWith(".resource.json") )
      .map(_.contentAsString)
      .map(read[ResourceMetadata])
      .toSeq

}

