package com.simplexportal.core

import com.typesafe.scalalogging.LazyLogging
import better.files._
import com.simplexportal.core.dao.Storage
import net.ceedubs.ficus.Ficus._
import com.typesafe.config.{Config, ConfigFactory}

object BuildStaticSite extends App with LazyLogging {

  val SIMPLEX_CORE_STORAGE = "simplex.core.storage"
  val SIMPLEX_CORE_OUTPUT = "simplex.core.output"

  lazy val config: Config = ConfigFactory.load()


  logger.info(s"Storage: ${config.as[String](SIMPLEX_CORE_STORAGE)}")
  logger.info(s"Output ${config.as[String](SIMPLEX_CORE_OUTPUT)}")

  val storage = new Storage(config.as[String](SIMPLEX_CORE_STORAGE))
  import storage._

  // Generate pages.
  storage.pages.foreach(page => {
    val pageFile = s"${config.as[String](SIMPLEX_CORE_OUTPUT)}${page.metadata.path}".toFile
    pageFile.parent.createDirectories()
    pageFile.write(Renderer.render(page))
  })

  // Copy resources.
  storage.collectResourceMetadata.foreach(metadata => {
    val resourceFile = s"${config.as[String](SIMPLEX_CORE_OUTPUT)}${metadata.path}".toFile
    resourceFile.parent.createDirectories()
    metadata.data.copyTo(resourceFile, true)
  })

}

