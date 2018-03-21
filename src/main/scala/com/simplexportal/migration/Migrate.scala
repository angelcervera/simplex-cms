package com.simplexportal.migration

import java.nio.file.{Path, Paths}

import better.files._
import net.ceedubs.ficus.Ficus._
import com.simplexportal.core.Configuration
import com.simplexportal.core.datamodel.Metadata._

import scala.xml.XML

object Migrate {

  def migrate(in: Path, out: Path) = {
    val exportRoot = out.toFile.toScala

    val backup = XML.load( (in.toString / "metadata.xml").url)
    println( s"Importing metadata backup ${(backup \ "code") text} to ${out.toAbsolutePath}")

    // Migrate pages
    backup \ "pages" \ "page" foreach( pageMetadata => {

      val path = (pageMetadata \ "path").text
      val template = (pageMetadata \ "template").text
      val pageRoot = ( exportRoot / s"cms/pages/$path" ) createIfNotExists(true, true)

      PageMetadata(path, template) toJson (pageRoot / "metadata.json")

      (pageMetadata \ "components" \ "component") foreach(componentMetadata => {
        val cmpName = (componentMetadata \ "name").text

        // Migrate metadata
        ComponentMetadata(
          `type` = (componentMetadata \ "type").text,
          name = cmpName,
          orderExecution = (componentMetadata \ "orderExecution").text.toInt,
          parameters = Map.empty // FIXME: extract properties (componentMetadata \ "parameters").map(node=>node.namespace->node.text).toMap
        ) toJson ( pageRoot / s"$cmpName.metadata.json" )


        // Migrate data
        val content = ( in.toString / s"pages${path}" / cmpName ).contentAsString
        ( pageRoot / s"$cmpName.data.html" ).write(content.substring(content.indexOf('>')+1, content.lastIndexOf('<')))

      })
    })

    // Migrate Templates.
    backup \ "templates" \ "template" foreach( templateMetadata => {
      val path = (templateMetadata \ "path").text
      val templateRoot = ( exportRoot / s"cms/templates${path}" ) createIfNotExists(true, true)

      // Migrate metadata
      TemplateMetadata(
        path = path,
        encoding = (templateMetadata \ "encoding").text,
        mimeType = (templateMetadata \ "mimeType").text
      ) toJson ( templateRoot / "metadata.json" )

      // Migrate data
      ( in.toString / s"templates${path}" ) copyTo( templateRoot / "data.html", true)

    })

    // Migrate static resources
    backup \ "resources" \ "resource" foreach( resourceMetadata => {
      val path = (resourceMetadata \ "path").text.toFile
      val resourceRoot = ( exportRoot / s"cms/resources/metadata${path.parent}" ) createIfNotExists(true, true)

      // Migrate metadata
      ResourceMetadata(
        path = (resourceMetadata \ "path").text,
        encoding = (resourceMetadata \ "encoding").text,
        mimeType = (resourceMetadata \ "mimeType").text
      ) toJson ( resourceRoot / s"${path.name}.metadata.json" )

      // Migrate data
      ( in.toString / s"resources${path}" ) copyTo( resourceRoot / path.name, true)

    })

  }

}

object MigrateMain extends App {

  Migrate.migrate(
    Paths.get(Configuration.config.as[String]("simplex.migration.in")),
    Paths.get(Configuration.config.as[String]("simplex.migration.out"))
  )

}
