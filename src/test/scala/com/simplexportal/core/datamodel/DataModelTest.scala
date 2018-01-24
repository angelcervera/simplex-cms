package com.simplexportal.core.datamodel

import org.scalatest.{ GivenWhenThen, WordSpec }

class DataModelTest extends WordSpec with GivenWhenThen {

  "Page" when {
    "try to extract the name" should {
      "return the right one" in {
        assert(Page("/page.html").name === "page.html")
        assert(Page("/folder1/folder2/page.html").name === "page.html")
      }
    }
    "try to extract the parent" should {
      "return the right one" in {
        assert(Page("/page.html").parent == Some("/"))
        assert(Page("/folder1/folder2/page.html").parent === Some("/folder1/folder2"))
      }
    }
    "try to create one with a wrong path" should {
      "throw an error" in {
        val thrown = intercept[IllegalArgumentException] { Page("/page.html/") }
        assert(thrown.getMessage === "requirement failed: Trying to create a content with invalid path [/page.html/]")

        val thrown2 = intercept[IllegalArgumentException] { Page("/page.html.") }
        assert(thrown2.getMessage === "requirement failed: Trying to create a content with invalid path [/page.html.]")
      }
    }
  }

  "Resource" when {
    "try to extract the name" should {
      "return the right one" in {
        assert(Resource("/img.png", "image/png").name === "img.png")
        assert(Resource("/folder1/folder2/img.png", "image/png").name === "img.png")
      }
    }
    "try to extract the parent" should {
      "return the right one" in {
        assert(Resource("/img.png", "image/png").parent == Some("/"))
        assert(Resource("/folder1/folder2/img.png", "image/png").parent === Some("/folder1/folder2"))
      }
    }
    "try to create one with a wrong path" should {
      "throw an error" in {
        val thrown = intercept[IllegalArgumentException] { Resource("/img.png/", "image/png") }
        assert(thrown.getMessage === "requirement failed: Trying to create a content with invalid path [/img.png/]")

        val thrown2 = intercept[IllegalArgumentException] { Resource("/img.png.", "image/png") }
        assert(thrown2.getMessage === "requirement failed: Trying to create a content with invalid path [/img.png.]")
      }
    }
  }

  "Folder" when {
    "try to extract the name" should {
      "return the right one" in {
        assert(Folder("/", None).name === "/")
        assert(Folder("/folder", None).name === "folder")
        assert(Folder("/folder1/folder2/folder3", None).name === "folder3")
      }
    }
    "try to extract the parent" should {
      "return the right one" in {
        assert(Folder("/", None).parent === None, "None for the root")
        assert(Folder("/folder", None).parent == Some("/"))
        assert(Folder("/folder1/folder2/folder3", None).parent === Some("/folder1/folder2"))
      }
    }
    "try to create one with a wrong path" should {
      "throw an error" in {
        val thrown = intercept[IllegalArgumentException] { Folder("/folder/", None) }
        assert(thrown.getMessage === "requirement failed: Trying to create a content with invalid path [/folder/]")
      }
    }
  }
}
