package com.simplexportal.core.dao

import better.files._
import better.files.Dsl.SymbolicOperations
import com.typesafe.scalalogging.LazyLogging
import org.json4s._
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.Serialization.writePretty
import org.json4s.native.Serialization.read

class FileSystemStorage(rootPath: String) extends Storage with LazyLogging {

  implicit def json4sFormats: Formats = DefaultFormats + new EnumNameSerializer(HttpCacheability)

  implicit class MetadataJsonUtilities(obj: Metadata) {
    def toJson: String = writePretty(obj)
    def toJson(file: File): File = {
      file.parent.createDirectories()
      file < toJson
    }
  }

  private def mkRelative(path: String) = if(path.startsWith("/")) "." + path else path

  private def createFileReferences(out: File, path: String): (File, File) = {
    val file: File = ( out / mkRelative(path) )
    val parent = file.parent.createDirectories()

    (file, parent)
  }

  implicit val formats = DefaultFormats

  lazy val rootFile = {
    val tmpFile = rootPath.toFile
    require(tmpFile.exists && tmpFile.isDirectory, s"${tmpFile} does not exist or it is not a directory")
    tmpFile
  }

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

  override def writePageMetadata(page: PageMetadata): Unit = {
    val (fileMeta, parentMeta) = createFileReferences( (rootFile / "meta") , page.path )
    page toJson (parentMeta / s"${fileMeta.name}.page.json")
  }

  override def collectPageMetadata =
    (rootFile / "meta")
      .listRecursively
      .filter(_.name.endsWith(".page.json") )
      .map(_.contentAsString)
      .map(read[PageMetadata])
      .toSeq

  override def writeResourceMetadata(resource: ResourceMetadata): Unit = {
    val (fileMeta, parentMeta) = createFileReferences( (rootFile / "meta") , resource.path )
    resource toJson (parentMeta / s"${fileMeta.name}.resource.json")
  }

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

