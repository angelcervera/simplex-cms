package com.simplexportal.core.datamodel

import org.scalatest.{ GivenWhenThen, WordSpec }

class DataModelTest extends WordSpec with GivenWhenThen {

  "Page" when {
    "calculates the name" should {
      "returns the right one" in {
        assert(Page("/page.html").name === "page.html")
        assert(Page("/folder1/folder2/page.html").name === "page.html")
      }
    }
    "calculates the parent" should {
      "returns the root if it is on the top" in assert(Page("/page.html").parent == Some("/"))
      "returns the right one" in assert(Page("/folder1/folder2/page.html").parent === Some("/folder1/folder2"))
    }
    "creates one with a wrong path" should {
      "throws an error" in {
        val thrown = intercept[IllegalArgumentException] { Page("/page.html/") }
        assert(thrown.getMessage === "requirement failed: Trying to create a content with invalid path [/page.html/]")

        val thrown2 = intercept[IllegalArgumentException] { Page("/page.html.") }
        assert(thrown2.getMessage === "requirement failed: Trying to create a content with invalid path [/page.html.]")
      }
    }
  }

  "Resource" when {
    "calculates the name" should {
      "returns the right one" in {
        assert(Resource("/img.png", "image/png").name === "img.png")
        assert(Resource("/folder1/folder2/img.png", "image/png").name === "img.png")
      }
    }
    "calculates the parent" should {
      "returns the root if it is on the top" in assert(Resource("/img.png", "image/png").parent == Some("/"))
      "returns the right one" in assert(Resource("/folder1/folder2/img.png", "image/png").parent === Some("/folder1/folder2"))
    }
    "creates one with a wrong path" should {
      "throws an error" in {
        val thrown = intercept[IllegalArgumentException] { Resource("/img.png/", "image/png") }
        assert(thrown.getMessage === "requirement failed: Trying to create a content with invalid path [/img.png/]")

        val thrown2 = intercept[IllegalArgumentException] { Resource("/img.png.", "image/png") }
        assert(thrown2.getMessage === "requirement failed: Trying to create a content with invalid path [/img.png.]")
      }
    }
  }

  "Folder" when {
    "calculates the name" should {
      "returns / if it is the root" in assert(Folder("/", None).name === "/")
      "returns the right one" in {
        assert(Folder("/folder", None).name === "folder")
        assert(Folder("/folder1/folder2/folder3", None).name === "folder3")
      }
    }
    "calculates the parent" should {
      "returns None if it is the root" in assert(Folder("/", None).parent === None)
      "returns the root if it is on the top" in assert(Folder("/folder", None).parent == Some("/"))
      "returns the right one" in assert(Folder("/folder1/folder2/folder3", None).parent === Some("/folder1/folder2"))
    }
    "creates one with a wrong path" should {
      "throws an error" in {
        val thrown = intercept[IllegalArgumentException] { Folder("/folder/", None) }
        assert(thrown.getMessage === "requirement failed: Trying to create a content with invalid path [/folder/]")
      }
    }
  }
}
