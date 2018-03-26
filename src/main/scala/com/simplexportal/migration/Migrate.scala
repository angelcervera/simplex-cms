package com.simplexportal.migration

import java.nio.file.{Path, Paths}

import better.files._
import net.ceedubs.ficus.Ficus._
import com.simplexportal.core.Configuration
import com.simplexportal.core.datamodel.Metadata._

import scala.xml.XML

object Migrate {

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

      val path = (xmlPageMetadata \ "path").text
      val template = (xmlPageMetadata \ "template").text

      val (fileMeta, parentMeta) = createFileReferences( (exportRoot / "meta") , path )
      PageMetadata(path, template) toJson (parentMeta / s"${fileMeta.name}.json")

      val (fileData, parentData) = createFileReferences( (exportRoot / "data"), path )
      (xmlPageMetadata \ "components" \ "component") foreach(xmlCmpMetadata => {
        val cmpName = (xmlCmpMetadata \ "name").text

        // Migrate metadata
        ComponentMetadata(
          `type` = (xmlCmpMetadata \ "type").text,
          name = cmpName,
          orderExecution = (xmlCmpMetadata \ "orderExecution").text.toInt,
          parameters = Map.empty // FIXME: extract properties (componentMetadata \ "parameters").map(node=>node.namespace->node.text).toMap
        ) toJson (parentMeta / s"${fileMeta.name}_${cmpName}.json")


        // Migrate data
        val content = ( in.toString / s"pages${path}" / cmpName ).contentAsString
        (parentData / s"_simplexportal_page_${fileData.name}_${cmpName}.html").write(content.substring(content.indexOf('>')+1, content.lastIndexOf('<')))

      })
    })


    // Migrate static resources
    backup \ "resources" \ "resource" foreach( xmlResourceMetadata => {
      val path = (xmlResourceMetadata \ "path").text
      val (fileMeta, parentMeta) = createFileReferences( (exportRoot / "meta"), path )

      // Migrate metadata
      ResourceMetadata(
        path = (xmlResourceMetadata \ "path").text,
        encoding = (xmlResourceMetadata \ "encoding").text,
        mimeType = (xmlResourceMetadata \ "mimeType").text
      ) toJson (parentMeta / s"${fileMeta.name}.json")

      // Migrate data
      val (fileData, parentData) = createFileReferences( (exportRoot / "data"), path )
      ( in.toString / s"resources${path}" ) copyTo( fileData, true )

    })

    // Migrate Templates.
    backup \ "templates" \ "template" foreach( xmlTemplateMetadata => {
      val path = (xmlTemplateMetadata \ "path").text
      val templatePath = (exportRoot / "templates" / mkRelative(path))

      // Migrate metadata
      TemplateMetadata(
        path = path,
        encoding = (xmlTemplateMetadata \ "encoding").text,
        mimeType = (xmlTemplateMetadata \ "mimeType").text
      ) toJson ( templatePath / "metadata.json" )

      // Migrate data
      ( in.toString / s"templates${path}" ) copyTo( ( templatePath / "data.html" ), true)

    })

  }

}

object MigrateMain extends App {

  Migrate.migrate(
    Paths.get(Configuration.config.as[String]("simplex.migration.in")),
    Paths.get(Configuration.config.as[String]("simplex.migration.out"))
  )

}
