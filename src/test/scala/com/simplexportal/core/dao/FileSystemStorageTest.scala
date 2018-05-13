package com.simplexportal.core.dao

import org.scalatest.WordSpec

class FileSystemStorageTest extends WordSpec {

  "FileSystemStorage" should {
    "return the full list of pages" in {
      val expected = Set(
        PageMetadata("/blog/index.html", HttpCache("1"), "/blog/list-posts.html","UTF-8"),
        PageMetadata("/blog/2015/10/04/post1/post.html", HttpCache("1"), "/blog/post.html","UTF-8")
      )
      val result = new FileSystemStorage("src/test/resources/com/simplexportal/tools/examples/blog/expected").collectPageMetadata

      assert(result.toSet == expected)
    }
    "return the full list of templates" in {
      val expected = Seq(
        TemplateMetadata("/blog/post.html","UTF-8","text/html"),
        TemplateMetadata("/blog/list-posts.html","UTF-8","text/html")
      )
      val result = new FileSystemStorage("src/test/resources/com/simplexportal/tools/examples/blog/expected").collectTemplateMetadata

      assert(result == expected)
    }
    "return the full list of resources" in {
      val result = new FileSystemStorage("src/test/resources/com/simplexportal/tools/examples/blog/expected").collectResourceMetadata
      assert(result.size == 20)
      assert(result.find(_.path == "/img/GitHub_Logo.png") == Some(ResourceMetadata("/img/GitHub_Logo.png", HttpCache("1"), "","image/png")))
      assert(result.find(_.path == "/index.html") == Some(ResourceMetadata("/index.html", HttpCache("1"), "UTF-8","text/html")))
    }
  }

}
