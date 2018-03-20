package com.simplexportal.core

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._

object Configuration {

  lazy val config: Config = ConfigFactory.load()


  def storage = config.as[String]("simplex.core.storage")
  def output  = config.as[String]("simplex.core.output")
}
