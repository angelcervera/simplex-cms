package com.simplexportal.migration

import java.nio.file.Paths

import org.scalatest.WordSpec

class MigrateTest extends WordSpec {

  "MigrateTest" should {

    "migrateFromFolder" in {
      Migrate.migratePages(Paths.get("src/test/resources/com/simplexportal/migration/examples/blog/input/unpackaged"), Paths.get("target/out/pages"))
    }

    "migrate" in {
      Migrate.migrate(
        Paths.get("com/simplexportal/migration/examples/blog/input/input.zip"),
        Paths.get("/target/out")
      )
    }

  }
}
