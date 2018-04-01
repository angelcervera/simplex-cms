package com.simplexportal.core

import com.typesafe.scalalogging.LazyLogging
import better.files._

object SimplexCore extends App with LazyLogging {


  logger.info(s"Storage: ${Configuration.storage}")
  logger.info(s"Output ${Configuration.output}")

  val storage = Storage(Configuration.storage)
  import storage._

  // Generate pages.
  storage.pages.foreach(page => {
    val pageFile = s"${Configuration.output}${page.metadata.path}".toFile
    pageFile.parent.createDirectories()
    pageFile.write(Renderer.render(page))
  })

  // Copy resources.
  storage.resources.foreach(metadata => {
    val resourceFile = s"${Configuration.output}${metadata.path}".toFile
    resourceFile.parent.createDirectories()
    metadata.data.copyTo(resourceFile, true)
  })

}

