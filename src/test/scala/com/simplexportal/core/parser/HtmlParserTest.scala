package com.simplexportal.core.parser

import org.scalatest.{ GivenWhenThen, WordSpec }
import org.scalatest.Matchers._
import better.files.File

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

  "HtmlParser" when {

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
            NodeLocation("section",Location(75,5,3165),Some(Location(170,5,9458)),1,Map("id" -> "content", "name"->"section1")),
            NodeLocation("header",Location(49,5,1885),Some(Location(73,5,3062)),1,Map("name"->"header1")),
            NodeLocation("slide",Location(63,9,2568),Some(Location(72,7,3040)),2,Map("id" -> "slide", "name"->"slide1")),
            NodeLocation("component",Location(50,9,1926),Some(Location(50,9,1926)),2,Map("name"->"component1"))
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
          NodeLocation("section",Location(75,5,3165),Some(Location(170,5,9458)),1,Map("id" -> "content", "name"->"section1")),
          NodeLocation("header",Location(49,5,1885),Some(Location(73,5,3062)),1,Map("name"->"header1"))
        ))
        result shouldBe expected
      }
    }

    "extracts the body of the node" should {
      "gives the right content" in {
        Given("A xhtml template")
        val html = File("src/test/resources/com/simplexportal/core/parser/template1.html").contentAsString
        When("search for a set of tags")
        val nodes = HtmlParser.searchComponents(html, 1)
        Then("return only the expected list of them")
        val expected = File("src/test/resources/com/simplexportal/core/parser/template1_head.body").contentAsString
        nodes match {
          case Right(nloc) => HtmlParser.extractBody(html, nloc.filter(_.`type` == "header").head) shouldBe expected
          case Left(e) => fail(e.toString)
        }
      }
    }

  }
}
