package com.simplexportal.tools

import better.files._
import com.simplexportal.core.dao.FileSystemStorage
import com.simplexportal.core.parser.Parser
import com.simplexportal.core.renderer.Renderer
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import net.ceedubs.ficus.Ficus._

object BuildStaticSite extends App with LazyLogging {

  val SIMPLEX_CORE_STORAGE = "simplex.core.storage"
  val SIMPLEX_CORE_OUTPUT = "simplex.core.output"

  lazy val config: Config = ConfigFactory.load()

  logger.info(s"Storage: ${config.as[String](SIMPLEX_CORE_STORAGE)}")
  logger.info(s"Output ${config.as[String](SIMPLEX_CORE_OUTPUT)}")

  val storage = new FileSystemStorage(config.as[String](SIMPLEX_CORE_STORAGE))
  import storage._

  val parser = new Parser(storage)

  // Generate pages.
  collectPageMetadata.foreach(page => {
    val pageFile = s"${config.as[String](SIMPLEX_CORE_OUTPUT)}${page.path}".toFile
    pageFile.parent.createDirectories()
    pageFile.write(Renderer.render(parser.treeNodes(page)))
  })

  // Copy resources.
  collectResourceMetadata.foreach(metadata => {
    val resourceFile = s"${config.as[String](SIMPLEX_CORE_OUTPUT)}${metadata.path}".toFile
    resourceFile.parent.createDirectories()
    storage.readResourceData(metadata).copyTo(resourceFile, true)
  })

}

