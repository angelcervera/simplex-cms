package com.simplexportal.core.renderer

import com.simplexportal.core.dao.{HttpCache, PageMetadata}
import org.scalatest.WordSpec

class MarkdownTransformerTest extends WordSpec {

  "MarkdownTransformer" should {
    "generate html" when {
      "it is used with a PageMetadata" in {
        assert( MarkdownTransformer.transform(PageMetadata("/", HttpCache("1"), "/template"), "This is *Sparta*" ) == Right("<p>This is <em>Sparta</em></p>\n"))
      }
    }

  }
}
