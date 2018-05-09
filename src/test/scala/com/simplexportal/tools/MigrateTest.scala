package com.simplexportal.tools

import java.nio.file.Paths

import com.simplexportal.core.dao.FolderMetadata
import org.scalatest.WordSpec

class MigrateTest extends WordSpec {

  "buildFolderMetadata" should {
    "build full folder metadata with defaultPage" in {
      val meta = Migrate.buildFolderMetadata(
        <folder>
          <defaultPage>/folder/content.html</defaultPage>
          <listContents>true</listContents>
          <path>/folder</path>
        </folder>
      )

      assert(meta == FolderMetadata("/folder", Some("/folder/content.html"), true))
    }
    "build with no defaultPage and no defaultResource" in {
      val meta = Migrate.buildFolderMetadata(
        <folder>
          <listContents>true</listContents>
          <path>/folder</path>
        </folder>
      )

      assert(meta == FolderMetadata("/folder", None, true))
    }
    "build with no listContents" in {
      val meta = Migrate.buildFolderMetadata(
        <folder>
          <path>/folder</path>
        </folder>
      )

      assert(meta == FolderMetadata("/folder", None, false))
    }
    "calculate the default content when defaultResource" in {
      val meta = Migrate.buildFolderMetadata(        <folder>
        <defaultResource>/index.html</defaultResource>
        <listContents>false</listContents>
        <path>/</path>
      </folder>)
      assert(meta == FolderMetadata("/", Some("/index.html"), false))
    }
  }

//  "MigrateTest" should {
//
//    "migrateFromFolder" in {
//      Migrate.migrate(Paths.get("src/test/resources/com/simplexportal/migration/examples/blog/input/unpackaged"), Paths.get("target/out/migration"))
//    }
//
//  }
}
