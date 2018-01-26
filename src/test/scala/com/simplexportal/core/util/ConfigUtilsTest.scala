package com.simplexportal.core.util

import com.typesafe.config.ConfigFactory
import org.scalatest.WordSpec

class ConfigUtilsTest extends WordSpec {

  "ConfigUtils" should {
    "generates a Properties object with full config" in {
      val conf = ConfigFactory.parseString(
        """
          | simplex-cms {
          |   velocity {
          |     engine {
          |       resource.loader = class
          |       class.resource.loader.description = Velocity Classpath Resource Loader
          |       class.resource.loader.class = org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
          |     }
          |     tools {
          |       prop1: "val1"
          |     }
          |   }
          | }
          |
        """.stripMargin)

      val expected = new java.util.Properties()
      expected.setProperty("resource.loader", "class")
      expected.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader")
      expected.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader")

      val result = conf.toProperties("simplex-cms.velocity.engine")

      assert(result === expected)
    }
  }

}
