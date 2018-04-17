package com.simplexportal.core

import com.simplexportal.core.parser.Parser
import org.scalatest.WordSpec

class RendererTest extends WordSpec {

  "Render" should {
    "render right page" when {
      "there are content before, after and in the middle" in {
        Parser.treeNodes("<html>is<simplex:c> this <simplex:d>renderer</simplex:d> working <simplex:e>properly</simplex:e> or </simplex:c>it isn't</html>") match {
          case Left(e) => fail(e.message)
          case Right(root) => assert(Renderer.render(root) == "<html>is this renderer working properly or it isn't</html>")
        }

      }
      "there is nothing before the first component" in {
        Parser.treeNodes("<html>is<simplex:c><simplex:d> this </simplex:d>renderer<simplex:e> working </simplex:e>properly</simplex:c> or it isn't</html>") match {
          case Left(e) => fail(e.message)
          case Right(root) => assert(Renderer.render(root) == "<html>is this renderer working properly or it isn't</html>")
        }
      }
      "there is nothing after the last component" in {
        Parser.treeNodes("<html>is<simplex:c> this <simplex:d>renderer</simplex:d> working <simplex:e>properly</simplex:e></simplex:c> or it isn't</html>") match {
          case Left(e) => fail(e.message)
          case Right(root) => assert(Renderer.render(root) == "<html>is this renderer working properly or it isn't</html>")
        }
      }
      "there is nothing between all components" in {
        Parser.treeNodes("<html>is<simplex:c><simplex:d> this </simplex:d><simplex:e>renderer</simplex:e></simplex:c> working properly or it isn't</html>") match {
          case Left(e) => fail(e.message)
          case Right(root) => assert(Renderer.render(root) == "<html>is this renderer working properly or it isn't</html>")
        }
      }
    }

  }

}
