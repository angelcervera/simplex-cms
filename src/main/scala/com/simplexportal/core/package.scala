package com.simplexportal.core

import com.typesafe.config.Config

import scala.collection.JavaConverters._

package object util {

  def toOption(strs: String*) = strs.find(s => s !=null && !s.isEmpty)

  /**
    * Utilities to manage TypeSafe Config
    */
  implicit class ConfigUtils(config: Config) {

    /**
      * Generate a Properties Java object from a path of properties.
      */
    implicit def toProperties(path: String) = {
      config.getConfig(path).entrySet().asScala
        .foldLeft(new java.util.Properties){ case(prop, entry) => {
          prop.setProperty(entry.getKey, entry.getValue.unwrapped().toString)
          prop
        }}
    }

  }
}
