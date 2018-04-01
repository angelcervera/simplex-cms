package com.simplexportal.core

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._

object Configuration {

  val SIMPLEX_CORE_STORAGE = "simplex.core.storage"
  val SIMPLEX_CORE_OUTPUT = "simplex.core.output"

  lazy val config: Config = ConfigFactory.load()


  def storage = config.as[String](SIMPLEX_CORE_STORAGE)
  def output  = config.as[String](SIMPLEX_CORE_OUTPUT)
}
