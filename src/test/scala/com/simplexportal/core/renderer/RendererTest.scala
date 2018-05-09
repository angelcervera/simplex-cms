package com.simplexportal.core.renderer

import com.simplexportal.core.dao.{HttpCache, PageMetadata, Storage, TemplateMetadata}
import com.simplexportal.core.parser.Parser
import org.scalamock.scalatest.MockFactory
import org.scalatest.WordSpec

class RendererTest extends WordSpec with MockFactory {

  def testTemplateRender(template: String, expected: String = "<html>is this renderer working properly or it isn't</html>") = {
    val pageMetadata = PageMetadata("/", HttpCache("1"), "/template")
    val templateMetadata = TemplateMetadata("/template", "UTF8", "text/html")
    val fakeStorage = stub[Storage]
    (fakeStorage.collectTemplateMetadata _).when().returns( Seq(templateMetadata))
    (fakeStorage.readTemplateData _)
      .when( where { _: TemplateMetadata => true} )
      .returns(template)
    (fakeStorage.collectComponentMetadata _).when( where { _: PageMetadata => true} ).returns(Seq.empty)

    val tree = new Parser(fakeStorage).treeNodes(pageMetadata)
    assert(Renderer.render(tree) == expected)
  }

  "Render" should {
    "render right page" when {
      "there are content before, after and in the middle" in {
        testTemplateRender("""<html>is<simplex:c name="nc"> this <simplex:d name="nd">renderer</simplex:d> working <simplex:e name="ne">properly</simplex:e> or </simplex:c>it isn't</html>""")
      }
      "there is nothing before the first component" in {
        testTemplateRender("""<html>is<simplex:c name="nc"><simplex:d name="nd"> this </simplex:d>renderer<simplex:e name="ne"> working </simplex:e>properly</simplex:c> or it isn't</html>""")
      }
      "there is nothing after the last component" in {
        testTemplateRender("""<html>is<simplex:c name="nc"> this <simplex:d name="nd">renderer</simplex:d> working <simplex:e name="ne">properly</simplex:e></simplex:c> or it isn't</html>""")
      }
      "there is nothing between all components" in {
        testTemplateRender("""<html>is<simplex:c name="nc"><simplex:d name="nd"> this </simplex:d><simplex:e name="ne">renderer</simplex:e></simplex:c> working properly or it isn't</html>""")
      }

      "there are markdown code" in {
        testTemplateRender(
          """<html>is<simplex:c name="nc"><simplex:d name="nd"> this </simplex:d><simplex:e name="ne" transformers="markdown">*renderer*</simplex:e></simplex:c> working properly or it isn't</html>""",
          "<html>is this <p><em>renderer</em></p>\n working properly or it isn't</html>"
        )
      }
    }

  }

}
