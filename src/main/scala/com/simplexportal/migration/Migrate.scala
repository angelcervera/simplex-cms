package com.simplexportal.migration

import java.nio.file.{Path, Paths}

import better.files._
import net.ceedubs.ficus.Ficus._
import com.simplexportal.core.Configuration

import scala.xml.XML

object Migrate {

  lazy val pp = new scala.xml.PrettyPrinter(Int.MaxValue, 4)

  def migrate(in: Path, out: Path) = {
    val exportRoot = out.toFile.toScala

    val backup = XML.load( (in.toString / "metadata.xml").url)
    println( s"Importing metadata backup ${(backup \ "code") text} to ${out.toAbsolutePath}")

    // Migrate pages
    backup \ "pages" \ "page" foreach( pageMetadata => {
      val path = (pageMetadata \ "path").text
      val template = (pageMetadata \ "template").text
      val pageRoot = ( exportRoot / s"cms/pages/$path" ) createIfNotExists(true, true)

      (pageRoot / "metadata.xml") write(pp.format( XML.loadString( s"<page><path>${path}</path><template>${template}</template></page>" ) ))

      (pageMetadata \ "components" \ "component") foreach(componentMetadata => {
        val cmpName = (componentMetadata \ "name").text

        // Migrate metadata
        ( pageRoot / s"$cmpName.metadata.xml" ) write(pp.format(componentMetadata))

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
      ( templateRoot / "metadata.xml" ) write(pp.format(templateMetadata))

      // Migrate data
      ( in.toString / s"templates${path}" ) copyTo( templateRoot / "data.html", true)

    })

    // Migrate static resources
    backup \ "resources" \ "resource" foreach( resourceMetadata => {
      val path = (resourceMetadata \ "path").text.toFile
      val resourceRoot = ( exportRoot / s"cms/resources/metadata${path.parent}" ) createIfNotExists(true, true)

      // Migrate metadata
      ( resourceRoot / s"${path.name}.metadata.xml" ) write(pp.format(resourceMetadata))

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
