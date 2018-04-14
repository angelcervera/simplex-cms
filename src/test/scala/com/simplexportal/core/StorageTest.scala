package com.simplexportal.core

import com.simplexportal.core.dao.DataModel.{ComponentMetadata, PageMetadata, TemplateMetadata}
import com.simplexportal.core.dao.{Component, Page, Storage, Template}
import org.scalatest.WordSpec

class StorageTest extends WordSpec {

  def cleanData(pages: Seq[Page]) = pages.map(page=>page.copy(
    components = page.components.map(cmp=>cmp.copy(data = "")),
    template = page.template.copy(data = "")
  ))

  "Storage" should {
      "return the full list of pages" in {
        val expected = Seq(
          Page(
            metadata = PageMetadata("/blog/index.html","/blog/list-posts.html","UTF-8"),
            components = Seq(
              Component(ComponentMetadata("content","post-content",100,Map()),"")
            ),
            template = Template(TemplateMetadata("/blog/list-posts.html","UTF-8","text/html"), "")
          ),
          Page(
            metadata = PageMetadata("/blog/2015/10/04/post1/post.html","/blog/post.html","UTF-8"),
            components = Seq(
              Component(ComponentMetadata("content","post-content",100,Map()),""),
              Component(ComponentMetadata("head","head",100,Map()),""),
              Component(ComponentMetadata("content","post-content-header",100,Map()),"")
            ),
            template = Template(TemplateMetadata("/blog/post.html","UTF-8","text/html"), "")
          )
        )
        val result = new Storage("src/test/resources/com/simplexportal/migration/examples/blog/expected").pages

        assert(cleanData(result) == expected)
      }
  }

}
