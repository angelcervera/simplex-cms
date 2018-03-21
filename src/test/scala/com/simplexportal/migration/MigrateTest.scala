package com.simplexportal.migration

import java.nio.file.Paths

import org.scalatest.WordSpec

class MigrateTest extends WordSpec {

  "MigrateTest" should {

    "migrateFromFolder" in {
      Migrate.migrate(Paths.get("src/test/resources/com/simplexportal/migration/examples/blog/input/unpackaged"), Paths.get("target/out/migration"))
    }

  }
}
