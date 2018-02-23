package com.simplexportal.core.parser

import better.files.File

import org.scalatest.Matchers._
import org.scalatest.{GivenWhenThen, WordSpec}

class HtmlParserTest extends WordSpec with GivenWhenThen {

//  def testParserResults(input: String, expected: List[NodeLocation]): Unit = {
//    Given("A xhtml template")
//    val html = File(input).contentAsString
//    When("search for components")
//    val result = Parser.listOfNodes(html)
//    Then("return only the expected list of them")
//    result match {
//      case Left(error) => fail(s"Threw ${error}")
//      case Right(r) => r shouldBe expected
//    }
//  }
//
//  "HtmlParser" should {
//
//    "search for components" should {
//      "find all components in a simple template" in {
//        testParserResults(
//          "src/test/resources/com/simplexportal/core/parser/simple_body_div.html",
//          List(
//            NodeLocation("body", Location(6,5,166),Some(Location(9,5,270))),
//            NodeLocation("div",Location(7,9,202),Some(Location(8,9,251))),
//            NodeLocation("head",Location(3,5,86),Some(Location(5,5,146)))
//          )
//        )
//      }
//
//      "find all components in a complex template" in {
//        testParserResults(
//          "src/test/resources/com/simplexportal/core/parser/template1.html",
//          List(
//            NodeLocation("nobody",Location(180,1,9756),Some(Location(180,1,9756))),
//            NodeLocation("emptybody",Location(179,1,9716),Some(Location(179,20,9735))),
//            NodeLocation("section",Location(75,5,3091),Some(Location(170,5,9289))),
//            NodeLocation("header",Location(49,5,1837),Some(Location(73,5,2990))),
//            NodeLocation("slide",Location(63,9,2506),Some(Location(72,7,2969))),
//            NodeLocation("component",Location(50,9,1877),Some(Location(50,9,1877)))
//          ))
//      }
//    }

//    "extracts the body of the node" should {
//      "give the right content" in {
//        Given("A xhtml template")
//        val html = File("src/test/resources/com/simplexportal/core/parser/template1.html").contentAsString
//        When("search for a set of tags")
//        val nodes = HtmlParser.searchNodes(html, 1)
//        Then("return only the expected body")
//        val expected = File("src/test/resources/com/simplexportal/core/parser/template1_header.body").contentAsString
//        nodes match {
//          case Right(nloc) => HtmlParser.extractBody(html, nloc.filter(_.`type` == "header").head) shouldBe expected
//          case Left(e) => fail(e.toString)
//        }
//      }
//
//      "give empty if no content" in {
//        Given("A xhtml template with a component without body")
//        val html = File("src/test/resources/com/simplexportal/core/parser/template1.html").contentAsString
//        When("search for all tags")
//        val nodes = HtmlParser.searchNodes(html)
//        Then("return a empty string.")
//        nodes match {
//          case Right(nloc) => {
//            HtmlParser.extractBody(html, nloc.filter(_.`type` == "emptybody").head) shouldBe ""
//            HtmlParser.extractBody(html, nloc.filter(_.`type` == "nobody").head) shouldBe ""
//          }
//          case Left(e) => fail(e.toString)
//        }
//      }
//    }
//
//    "extract right body in a component" when {
//      "the body is constant" in {
//        val result = HtmlParser.extractBody(ComponentMetadata(
//          "id",
//          "cnt",
//          "<simplex:cnt id=\"component1\">Constant value</simplex:cnt>",
//          1
//        ))
//
//        assert(result === "Constant value")
//      }
//      "the body contains other components" in {
//        val result = HtmlParser.extractBody(ComponentMetadata(
//          "id",
//          "cnt",
//          "<simplex:cnt id=\"component1\">Other components <simplex:cnt id=\"component2\">like this</simplex:cnt>inside</simplex:cnt>",
//          1
//        ))
//
//        assert(result === "Other components <simplex:cnt id=\"component2\">like this</simplex:cnt>inside")
//      }
//      "it is no body tag" in {
//        val result = HtmlParser.extractBody(ComponentMetadata(
//          "id",
//          "cnt",
//          "<simplex:cnt id=\"component1\"/>",
//          1
//        ))
//
//        assert(result === "")
//      }
//      "the body is empty" in {
//        val result = HtmlParser.extractBody(ComponentMetadata(
//          "id",
//          "cnt",
//          "<simplex:cnt id=\"component1\"></simplex:cnt>",
//          1
//        ))
//
//        assert(result === "")
//      }
//    }
//  }
}
