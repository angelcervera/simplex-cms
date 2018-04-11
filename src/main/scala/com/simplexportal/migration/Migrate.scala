package com.simplexportal.migration

import java.nio.file.{Path, Paths}

import net.ceedubs.ficus.Ficus._
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.writePretty
import better.files._
import better.files.Dsl.SymbolicOperations
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try
import scala.xml.{Node, XML}

import com.simplexportal.core.datamodel.Metadata._
import com.simplexportal.core.util._

object Migrate {

  implicit val formats = Serialization.formats(NoTypeHints)

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

  def migrate(in: Path, out: Path) = {
    val exportRoot = out.toFile.toScala  createDirectories


    val backup = XML.load( (in.toString / "metadata.xml").url)
    println( s"Importing metadata backup ${(backup \ "code") text} to ${out.toAbsolutePath}")

    // Migrate pages
    backup \ "pages" \ "page" foreach( xmlPageMetadata => {

      val pageMeta = buildPageMetadata(xmlPageMetadata)

      val (fileMeta, parentMeta) = createFileReferences( (exportRoot / "meta") , pageMeta.path )
      pageMeta toJson (parentMeta / s"${fileMeta.name}.page.json")

      val (fileData, parentData) = createFileReferences( (exportRoot / "data"), pageMeta.path )
      (xmlPageMetadata \ "components" \ "component") foreach(xmlCmpMetadata => {
        val cmpMeta = buildComponentMetadata(xmlCmpMetadata)

        // Migrate metadata
        cmpMeta toJson (parentMeta / s"${fileMeta.name}_${cmpMeta.name}.component.json")


        // Migrate data
        val content = ( in.toString / s"pages${pageMeta.path}" / cmpMeta.name ).contentAsString
        (parentData / s"_simplexportal_page_${fileData.name}_${cmpMeta.name}.html").write(content.substring(content.indexOf('>')+1, content.lastIndexOf('<')))

      })
    })


    // Migrate static resources
    backup \ "resources" \ "resource" foreach( xmlResourceMetadata => {

      // Migrate metadata
      val meta = buildResourceMetadata(xmlResourceMetadata)
      val (fileMeta, parentMeta) = createFileReferences( (exportRoot / "meta"), meta.path )
      meta toJson (parentMeta / s"${fileMeta.name}.resource.json")

      // Migrate data
      val (fileData, parentData) = createFileReferences( (exportRoot / "data"), meta.path )
      ( in.toString / s"resources${meta.path}" ) copyTo( fileData, true )

    })

    // Migrate Templates.
    backup \ "templates" \ "template" foreach( xmlTemplateMetadata => {

      // Migrate metadata
      val meta = buildTemplateMetadata(xmlTemplateMetadata)
      val templatePath = (exportRoot / "templates" / mkRelative(meta.path))
      meta toJson ( templatePath / "metadata.json" )

      // Migrate data
      ( in.toString / s"templates${meta.path}" ) copyTo( ( templatePath / "data.html" ), true)

    })

    // Migrate Folders.
    backup \ "folders" \ "folder" foreach( xmlFolderMetadata => {
      val meta = buildFolderMetadata(xmlFolderMetadata)
      meta toJson (exportRoot / "meta" / mkRelative(meta.path) / "folder.json")
    })
  }

  def buildFolderMetadata(xml: Node) =
    FolderMetadata(
      path = (xml \ "path").text,
      defaultContent = toOption((xml \ "defaultPage").text, (xml \ "defaultResource").text),
      listContents = Try((xml \ "listContents").text.toBoolean).getOrElse(false)
    )

  def buildResourceMetadata(xml: Node) =
    ResourceMetadata(
      path = (xml \ "path").text,
      encoding = (xml \ "encoding").text,
      mimeType = (xml \ "mimeType").text
    )

  def buildTemplateMetadata(xml: Node) =
    TemplateMetadata(
      path = (xml \ "path").text,
      encoding = (xml \ "encoding").text,
      mimeType = (xml \ "mimeType").text
    )

  def buildPageMetadata(xml: Node) =
    PageMetadata(
      path = (xml \ "path").text,
      template = (xml \ "template").text,
      encoding = Option((xml \ "encoding").text).getOrElse("UTF-8")
    )

  def buildComponentMetadata(xml: Node) =
    ComponentMetadata(
      `type` = (xml \ "type").text,
      name = (xml \ "name").text,
      orderExecution = (xml \ "orderExecution").text.toInt,
      parameters = Map.empty // FIXME: extract properties (componentMetadata \ "parameters").map(node=>node.namespace->node.text).toMap
    )


}

object MigrateMain extends App {

  val config: Config = ConfigFactory.load()

  Migrate.migrate(
    Paths.get(config.as[String]("simplex.migration.in")),
    Paths.get(config.as[String]("simplex.migration.out"))
  )

}
