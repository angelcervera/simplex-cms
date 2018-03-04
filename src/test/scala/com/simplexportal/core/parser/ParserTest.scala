package com.simplexportal.core.parser

import org.scalatest.{GivenWhenThen, WordSpec}
import better.files.File
import com.simplexportal.core.parser.Parser.PartialNode
import org.scalatest.Matchers._

class ParserTest extends WordSpec with GivenWhenThen {

  implicit  class SimplexPortalNodeTestHelpers(node: SimplexPortalNode) {
    implicit def cleanTemplateFragments: SimplexPortalNode = node.copy(templateFragments = List.empty, children = node.children.map(_.cleanTemplateFragments))
  }

  def testParserResults(template: String, expected: SimplexPortalNode): Unit = Parser.treeNodes(template) match {
    case Left(error) => fail(s"Threw ${error}")
    case Right(r) => assert(r == expected, s"\nExpected:\n${expected.stringAsTree()}\nCurrent:\n${r.stringAsTree()}")
  }

  def testParserResultsNoContent(template: String, expected: SimplexPortalNode): Unit = Parser.treeNodes(template) match {
    case Left(error) => fail(s"Threw ${error}")
    case Right(r) => assert(r.cleanTemplateFragments == expected.cleanTemplateFragments, s"\nExpected:\n${expected.stringAsTree()}\nCurrent:\n${r.stringAsTree()}")
  }





  val simpleBodyDivTemplate = File("src/test/resources/com/simplexportal/core/parser/simple_body_div.html").contentAsString
  val simpleBodyDivTree =
    SimplexPortalNode("root",Location(1,1,0),Location(16,1,461), Map.empty, List(
      SimplexPortalNode("body", Location(3,5,86),Location(11,5,352), Map("name" -> "body1"), List(
        SimplexPortalNode("div",Location(4,9,122),Location(10,9,333), Map("class" -> "bg-top", "name" -> "div1"), List(
          SimplexPortalNode("div",Location(6,13,203),Location(8,13,283), Map("class" -> "bg-top", "name" -> "div2"), List.empty, List.empty)), List.empty
        )
      ), List.empty),
      SimplexPortalNode("emptybody",Location(13,5,393),Location(13,24,412), Map.empty, List.empty, List.empty),
      SimplexPortalNode("nobody",Location(13,46,434),Location(13,46,434), Map.empty, List.empty, List.empty)
    ), List.empty)

  val partialDiv1 = PartialNode("div",Location(4,9,122),Some(Location(10,9,333)), Map("class" -> "bg-top", "name" -> "div1"), List(
    PartialNode("div",Location(6,13,203),Some(Location(8,13,283)), Map("class" -> "bg-top", "name" -> "div2"), List.empty))
  )
  val simpleBodyDivTreePartialNodes =
    PartialNode("root",Location(1,1,0),Some(Location(16,1,461)), Map.empty, List(
      PartialNode("body", Location(3,5,86),Some(Location(11,5,352)), Map("name" -> "body1"), List(partialDiv1)),
      PartialNode("emptybody",Location(13,5,393),Some(Location(13,24,412)), Map.empty, List.empty),
      PartialNode("nobody",Location(13,46,434),Some(Location(13,46,434)), Map.empty, List.empty)
    ))





  val template1Template = File("src/test/resources/com/simplexportal/core/parser/template1.html").contentAsString
  val template1Tree =
    SimplexPortalNode("root",Location(1,1,0),Location(182,8,9789), Map.empty, List(
      SimplexPortalNode("header",Location(49,5,1837),Location(73,5,2990), Map("name"->"header1"), List(
        SimplexPortalNode("component",Location(50,9,1877),Location(50,9,1877), Map("name"->"component1"), List.empty, List.empty),
        SimplexPortalNode("slide",Location(63,9,2506),Location(72,7,2969), Map("id"->"slide", "name"->"slide1"), List.empty, List.empty)
      ), List.empty),
      SimplexPortalNode("section",Location(75,5,3091),Location(170,5,9289), Map("id"->"content","name"->"section1"), List.empty, List.empty),
      SimplexPortalNode("emptybody",Location(179,1,9716),Location(179,20,9735), Map.empty, List.empty, List.empty),
      SimplexPortalNode("nobody",Location(180,1,9756),Location(180,1,9756), Map.empty, List.empty, List.empty)
    ), List.empty)

    "tree nodes" when {
      "search for node" should {
        "find all of them in a simple template" in {
          testParserResultsNoContent( simpleBodyDivTemplate, simpleBodyDivTree )
        }

        "find all of them in a complex template" in {
          testParserResultsNoContent( template1Template, template1Tree )
        }
      }
    }

  "fragments" should {
    "extract all fragments" when {
      "root contains 3 components and text around them" in {
        val fragments = Parser.fragments(simpleBodyDivTemplate, simpleBodyDivTreePartialNodes)
        assert(fragments.size == 3)
        assert(fragments(0) == "<!DOCTYPE html>\n<html lang=\"en\" xmlns:simplex=\"http://simplexportal.com/simplex\">\n    ")
        assert(fragments(1) == "\n    <p>paragraph</p>\n    ")
        assert(fragments(2) == "\n</html>\n\n")
      }
      "inner component contains 1 component and text around it" in {
        val fragments = Parser.fragments(simpleBodyDivTemplate, partialDiv1)
        assert(fragments.size == 2)
        assert(fragments(0) == "\n            bodyOfDiv_start\n            ")
        assert(fragments(1) == "\n            bodyOfDiv1_end\n        ")
      }
    }

  }

}
