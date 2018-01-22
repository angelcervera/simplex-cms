package com.simplexportal.core.parser

import org.scalatest.{ GivenWhenThen, WordSpec }
import better.files.File

import com.simplexportal.core.parser.HtmlParser.{ Location, NodeLocation }

class HtmlParserTest extends WordSpec with GivenWhenThen {

  "HtmlParser" when {
    "search for tags and namespaces" should {

      def testParserResults(input: String, tags: Seq[String], namespaces: Seq[String], expected: List[NodeLocation]): Unit = {
        Given("A xhtml template")
        val htmlStream = File(input).newFileReader
        When("search for a set of tags")
        val result = HtmlParser.searchTags(htmlStream, tags, namespaces)
        Then("return only the expected list of them")
        result match {
          case Left(error) => fail(s"Threw ${error}")
          case Right(r) => assert(r == expected)
        }
      }

      "find all head/body/div in a simple template" in {
        testParserResults(
          "src/test/resources/com/simplexportal/core/parser/simple_head_body.html",
          Seq("head", "body", "div"),
          Seq(HtmlParser.SIMPLEX_NS_PREFIX),
          List(
            NodeLocation("body", Location(6, 5, 88), Some(Location(9, 5, 143)), 1),
            NodeLocation("div", Location(7, 9, 103), Some(Location(8, 9, 132)), 2),
            NodeLocation("head", Location(3, 5, 37), Some(Location(5, 5, 76)), 1)))
      }

      "find all head/body tags in a complex template" in {
        testParserResults(
          "src/test/resources/com/simplexportal/core/parser/template1.html",
          Seq("head", "body"),
          Seq(HtmlParser.SIMPLEX_NS_PREFIX),
          List(
            NodeLocation("body", Location(45, 1, 1752), Some(Location(179, 1, 9792)), 1),
            NodeLocation("component", Location(50, 9, 1903), Some(Location(50, 9, 1903)), 2),
            NodeLocation("head", Location(3, 1, 84), Some(Location(44, 1, 1743)), 1)))
      }

      "find all level head/body/a tags in a complex template" in {
        Given("A xhtml template")
        val htmlStream = File("src/test/resources/com/simplexportal/core/parser/template1.html").newFileReader
        When("search for a set of tags")
        val result = HtmlParser.searchTags(htmlStream, Seq("head", "body", "a"), Seq(HtmlParser.SIMPLEX_NS_PREFIX))
        Then("return only the expected list of them")
        result match {
          case Left(error) => fail(s"Threw ${error}")
          case Right(r) => assert(r.size == 24)
        }
      }
    }

    "search for tags in the first level" should {
      "find all level head/body/a tags in a complex template" in {
        Given("A xhtml template")
        val htmlStream = File("src/test/resources/com/simplexportal/core/parser/template1.html").newFileReader
        When("search for a set of tags")
        val result = HtmlParser.searchTags(htmlStream, Seq("head", "body", "a"), Seq(HtmlParser.SIMPLEX_NS_PREFIX), 1)
        Then("return only the expected list of them")
        val expected = Right(List(
          NodeLocation("body", Location(45, 1, 1752), Some(Location(179, 1, 9792)), 1),
          NodeLocation("head", Location(3, 1, 84), Some(Location(44, 1, 1743)), 1)))
        assert(result == expected)
      }
    }

  }
}
