package com.simplexportal.migration

import java.nio.file.Path

import better.files._

import scala.xml.XML

object Migrate {

  lazy val pp = new scala.xml.PrettyPrinter(Int.MaxValue, 4)

  def migratePages(in: Path, out: Path) = {
    val exportRoot = out.toFile.toScala

    val backup = XML.load( (in.toString / "metadata.xml").url)
    println( s"Importing metadata backup ${(backup \ "code") text} to ${out.toAbsolutePath}")

    backup \ "pages" \ "page" foreach( page => {
      val path = (page \ "path").text
      val pageRoot = ( exportRoot / s"cms/pages/$path" ) createIfNotExists(true, true)
      (page \ "components" \ "component") foreach(component => {
        val cmpName = (component \ "name").text

        // Migrate metadata
        ( pageRoot / s"$cmpName.metadata.xml" ).write(pp.format(component))

        // Migrate data
        val content = ( in.toString / s"pages${path}" / cmpName ).contentAsString
        ( pageRoot / s"$cmpName.data.html" ).write(content.substring(content.indexOf('>')+1, content.lastIndexOf('<')))

      })
    })
  }

  def migrate(in: Path, outPath: Path) = {

  }

}
