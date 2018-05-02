package com.simplexportal.core.dao

import better.files._
import com.simplexportal.core.dao._
import com.typesafe.scalalogging.LazyLogging
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.read

class FileSystemStorage(rootPath: String) extends Storage with LazyLogging {

  implicit val formats = DefaultFormats

//  implicit class ResourceMetadataEnrich(metadata: ResourceMetadata) {
//    def data = ( rootFile / s"data/${metadata.path}" )
//  }
//
//  implicit class TemplateMetadataEnrich(metadata: TemplateMetadata) {
//    def data = (rootFile / s"templates${metadata.path}/data.html")
//  }


  lazy val rootFile = {
    val tmpFile = rootPath.toFile
    require(tmpFile.exists && tmpFile.isDirectory, s"${tmpFile} does not exist or it is not a directory")
    tmpFile
  }

//  lazy val templates = readTemplates

//  def paths =
//    pages.map(p=>p.metadata.path -> p).toMap ++
//    resources.map(r=>r.metadata.path -> r).toMap ++
//    folders.map(f=>f.metadata.path -> f).toMap

//  def pages: Seq[Page] =
//    collectPageMetadata.foldLeft(Seq.empty[Page]) { (s, m) =>
//      templates.get(m.template) match {
//        case None =>
//          logger.error(s"Ignoring page [${m.path}] because is using  the template [${m.template}] and it is not defined.")
//          s
//        case Some(templ) => s :+ Page(m, readComponents(m), templ)
//      }
//    }

//  def resources: Seq[Resource] =
//    collectResourceMetadata
//      .map(metadata => Resource(metadata))
//
//  def folders: Seq[Folder] =
//    collectFolderMetadata
//      .map(metadata => Folder(metadata))

//  def readTemplates: Map[String, Template] =
//      collectTemplateMetadata
//      .map(template => template.path -> Template(template, template.data.contentAsString))
//      .toMap

//  def readComponents(pageMetadata: PageMetadata): Seq[Component] = {
//    val absolutePath = (rootFile / s"meta${pageMetadata.path}")
//    val pageName = absolutePath.name
//    absolutePath
//      .parent
//      .list
//      .filter(f => f.name.startsWith(pageName) && f.name.endsWith(".component.json"))
//      .map(_.contentAsString)
//      .map(read[ComponentMetadata])
//      .map(cmp => Component(cmp, readComponentData(cmp).contentAsString))
//      .toSeq
//  }


  override def readTemplateData(metadata: TemplateMetadata) = (rootFile / s"templates${metadata.path}/data.html").contentAsString

  override def readComponentData(cmp: ComponentMetadata) = (rootFile / s"data${cmp.path}").contentAsString

  override def readResourceData(resource: ResourceMetadata) =  (rootFile / s"data${resource.path}")


  override def collectComponentMetadata(pageMetadata: PageMetadata) = {
    val absolutePath = (rootFile / s"meta${pageMetadata.path}")
    val pageName = absolutePath.name
    absolutePath
      .parent
      .list
      .filter(f => f.name.startsWith(pageName) && f.name.endsWith(".component.json"))
      .map(_.contentAsString)
      .map(read[ComponentMetadata])
      .toSeq
  }

  override def collectPageMetadata =
    (rootFile / "meta")
      .listRecursively
      .filter(_.name.endsWith(".page.json") )
      .map(_.contentAsString)
      .map(read[PageMetadata])
      .toSeq

  override def collectResourceMetadata =
    (rootFile / "meta")
      .listRecursively
      .filter(_.name.endsWith(".resource.json") )
      .map(_.contentAsString)
      .map(read[ResourceMetadata])
      .toSeq

  override def collectTemplateMetadata =
    (rootFile / "templates")
      .listRecursively
      .filter(_.name == "metadata.json" )
      .map(_.contentAsString)
      .map(read[TemplateMetadata])
      .toSeq

  override def collectFolderMetadata =
    (rootFile / "meta")
      .listRecursively
      .filter(_.name == "folder.json" )
      .map(_.contentAsString)
      .map(read[FolderMetadata])
      .toSeq

}

