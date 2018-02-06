package com.simplexportal.core.parser

import org.scalatest.{GivenWhenThen, WordSpec}
import org.scalatest.Matchers._
import better.files.File
import com.simplexportal.core.component.ComponentMetadata

class HtmlParserTest extends WordSpec with GivenWhenThen {

  def testParserResults(input: String, expected: List[NodeLocation]): Unit = {
    Given("A xhtml template")
    val html = File(input).contentAsString
    When("search for components")
    val result = HtmlParser.searchComponents(html)
    Then("return only the expected list of them")
    result match {
      case Left(error) => fail(s"Threw ${error}")
      case Right(r) => r shouldBe expected
    }
  }

  "HtmlParser" should {

    "search for components" should {
      "find all components in a simple template" in {
        testParserResults(
          "src/test/resources/com/simplexportal/core/parser/simple_head_body.html",
          List(
            NodeLocation("body", Location(6,5,166),Some(Location(9,5,270)),1,Map("name"->"body1")),
            NodeLocation("div",Location(7,9,202),Some(Location(8,9,251)),2,Map("name"->"div1", "class"->"bg-top")),
            NodeLocation("head",Location(3,5,86),Some(Location(5,5,146)),1,Map("name"->"head1"))
          )
        )
      }

      "find all components in a complex template" in {
        testParserResults(
          "src/test/resources/com/simplexportal/core/parser/template1.html",
          List(
            NodeLocation("nobody",Location(180,1,9756),Some(Location(180,1,9756)),1,Map()),
            NodeLocation("emptybody",Location(179,1,9716),Some(Location(179,20,9735)),1,Map()),
            NodeLocation("section",Location(75,5,3091),Some(Location(170,5,9289)),1,Map("id" -> "content", "name"->"section1")),
            NodeLocation("header",Location(49,5,1837),Some(Location(73,5,2990)),1,Map("name"->"header1")),
            NodeLocation("slide",Location(63,9,2506),Some(Location(72,7,2969)),2,Map("id" -> "slide", "name"->"slide1")),
            NodeLocation("component",Location(50,9,1877),Some(Location(50,9,1877)),2,Map("name"->"component1"))
          ))
      }
    }

    "search for first level components" should {
      "find two" in {
        Given("A xhtml template")
        val html = File("src/test/resources/com/simplexportal/core/parser/template1.html").contentAsString
        When("search for a set of tags")
        val result = HtmlParser.searchComponents(html, 1)
        Then("return only the expected list of them")
        val expected = Right(List(
          NodeLocation("nobody",Location(180,1,9756),Some(Location(180,1,9756)),1,Map()),
          NodeLocation("emptybody",Location(179,1,9716),Some(Location(179,20,9735)),1,Map()),
          NodeLocation("section",Location(75,5,3091),Some(Location(170,5,9289)),1,Map("id" -> "content", "name" -> "section1")),
          NodeLocation("header",Location(49,5,1837),Some(Location(73,5,2990)),1,Map("name" -> "header1"))
        ))
        result shouldBe expected
      }
    }

    "extracts the body of the node" should {
      "give the right content" in {
        Given("A xhtml template")
        val html = File("src/test/resources/com/simplexportal/core/parser/template1.html").contentAsString
        When("search for a set of tags")
        val nodes = HtmlParser.searchComponents(html, 1)
        Then("return only the expected body")
        val expected = File("src/test/resources/com/simplexportal/core/parser/template1_header.body").contentAsString
        nodes match {
          case Right(nloc) => HtmlParser.extractBody(html, nloc.filter(_.`type` == "header").head) shouldBe expected
          case Left(e) => fail(e.toString)
        }
      }

      "give empty if no content" in {
        Given("A xhtml template with a component without body")
        val html = File("src/test/resources/com/simplexportal/core/parser/template1.html").contentAsString
        When("search for all tags")
        val nodes = HtmlParser.searchComponents(html)
        Then("return a empty string.")
        nodes match {
          case Right(nloc) => {
            HtmlParser.extractBody(html, nloc.filter(_.`type` == "emptybody").head) shouldBe ""
            HtmlParser.extractBody(html, nloc.filter(_.`type` == "nobody").head) shouldBe ""
          }
          case Left(e) => fail(e.toString)
        }
      }
    }

    "extract right body in a component" when {
      "the body is constant" in {
        val result = HtmlParser.extractBody(ComponentMetadata(
          "id",
          "cnt",
          "<simplex:cnt id=\"component1\">Constant value</simplex:cnt>",
          1
        ))

        assert(result === "Constant value")
      }
      "the body contains other components" in {
        val result = HtmlParser.extractBody(ComponentMetadata(
          "id",
          "cnt",
          "<simplex:cnt id=\"component1\">Other components <simplex:cnt id=\"component2\">like this</simplex:cnt>inside</simplex:cnt>",
          1
        ))

        assert(result === "Other components <simplex:cnt id=\"component2\">like this</simplex:cnt>inside")
      }
      "it is no body tag" in {
        val result = HtmlParser.extractBody(ComponentMetadata(
          "id",
          "cnt",
          "<simplex:cnt id=\"component1\"/>",
          1
        ))

        assert(result === "")
      }
      "the body is empty" in {
        val result = HtmlParser.extractBody(ComponentMetadata(
          "id",
          "cnt",
          "<simplex:cnt id=\"component1\"></simplex:cnt>",
          1
        ))

        assert(result === "")
      }
    }
  }
}
