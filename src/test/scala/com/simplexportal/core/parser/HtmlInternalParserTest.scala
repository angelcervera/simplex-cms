package com.simplexportal.core.parser

import better.files.File
import org.scalatest.WordSpec

import HtmlInternalParser._

class HtmlInternalParserTest extends WordSpec {

  val Q = "\""
  implicit class SimplexPortalNodeTestHelpers(node: PartialNode) {

    implicit def cleanTemplateFragments: PartialNode = node.copy(children = node.children.map(_.cleanTemplateFragments))

    implicit def definition: String = {
      def location(sl: Option[Location]) = sl.map(l=>s"Location(${l.line},${l.column},${l.characterOffset})").toString
      def parameters(params: Map[String, String]) = params.toList match {
        case Nil => "Map.empty"
        case m => m.map{case (k,v) => s"$Q$k$Q -> $Q$v$Q"} mkString ("Map(", ",",")")
      }
      def children(nodes: List[PartialNode]) = nodes match {
        case Nil => "List.empty"
        case _ => nodes.map(_.definition).mkString("List(",",",")")
      }
      s"SimplexPortalNode($Q${node.`type`}$Q, ${location(Option(node.start))}, ${location(node.end)}, ${parameters(node.parameters)}, ${children(node.children)}, List.empty)"
    }

    implicit def findByName(name:String): Option[PartialNode] = {

      def onChildren(name: String, nodes: Seq[PartialNode]): Option[PartialNode] = nodes match {
        case Nil => None
        case head::tail => head.findByName(name).orElse(onChildren(name, tail))
      }

      node.parameters.get("name") match {
        case Some(n) if n==name => Some(node)
        case _ => onChildren(name, node.children)
      }

    }

  }

  def testParserResults(template: String, expected: PartialNode): Unit = buildInternalTree(template) match {
    case Left(error) => fail(s"Threw ${error}")
    case Right(r) => assert(r == expected, s"\nExpected:\n${expected.definition}\nCurrent:\n${r.definition}")
  }

  def testParserResultsNoContent(template: String, expected: PartialNode): Unit = buildInternalTree(template) match {
    case Left(error) => fail(s"Threw ${error}")
    case Right(r) => assert(r.cleanTemplateFragments == expected.cleanTemplateFragments, s"\nExpected:\n${expected.cleanTemplateFragments.definition}\nCurrent:\n${r.cleanTemplateFragments.definition}")
  }

  val template1Template = File("src/test/resources/com/simplexportal/core/parser/template1.html").contentAsString
  val template1Tree =
    PartialNode("root",Location(1,1,0),Some(Location(182,8,9789)), Map.empty, List(
      PartialNode("header",Location(49,5,1837),Some(Location(73,5,2990)), Map("name"->"header1"), List(
        PartialNode("component",Location(50,9,1877),Some(Location(50,9,1877)), Map("name"->"component1"), List.empty),
        PartialNode("slide",Location(63,9,2506),Some(Location(72,7,2969)), Map("id"->"slide", "name"->"slide1"), List.empty)
      )),
      PartialNode("section",Location(75,5,3091),Some(Location(170,5,9289)), Map("id"->"content","name"->"section1"), List.empty),
      PartialNode("emptybody",Location(179,1,9716),Some(Location(179,20,9735)), Map.empty, List.empty),
      PartialNode("nobody",Location(180,1,9756),Some(Location(180,1,9756)), Map.empty, List.empty)
    ))

  val simpleBodyDivTemplate = File("src/test/resources/com/simplexportal/core/parser/simple_body_div.html").contentAsString
  val simpleBodyDivTree =
    PartialNode("root", Location(1,1,0), Some(Location(17,1,519)), Map.empty, List(
      PartialNode("body", Location(3,5,86), Some(Location(12,5,379)), Map("name" -> "body1"), List(
        PartialNode("div", Location(4,9,122), Some(Location(10,9,333)), Map("class" -> "bg-top","name" -> "div1"), List(
          PartialNode("div", Location(6,13,203), Some(Location(8,13,283)), Map("class" -> "bg-top","name" -> "div2"), List.empty)
        )),
        PartialNode("nobody2", Location(11,9,356), Some(Location(11,9,356)), Map.empty, List.empty)
      )),
      PartialNode("emptybody", Location(14,5,420), Some(Location(14,41,456)), Map("name" -> "emptybody"), List.empty),
      PartialNode("nobody", Location(14,63,478), Some(Location(14,63,478)), Map("name" -> "nobody"), List.empty)
    ))

    "treeFromTemplate" when {
      "search for node" should {
        "find all of them in a simple template" in {
          testParserResultsNoContent(simpleBodyDivTemplate, simpleBodyDivTree)
        }

        "find all of them in a complex template" in {
          testParserResultsNoContent(template1Template, template1Tree)
        }
      }
      "respect init and the end of the template, out of components" in {
        treeFromTemplate("""<html>start<simplex:c name="nc">c<simplex:d name="nd">d</simplex:d>c<simplex:e name="ne">e</simplex:e>c</simplex:c>end</html>""") match {
          case Right(tree) => {
            assert(tree.children(0).textFragments == List("c", "c", "c"))
            assert(tree.textFragments == List("<html>start", "end</html>"))
          }
          case Left(error) => fail(error.message)
        }
      }
    }

  "treeFromComponent" when {
      "generate num. components +1 fragments" when {
        "there are content before, after and in the middle" in {
          treeFromComponent("""<simplex:c name="nc">c<simplex:d name="nd">d</simplex:d>c<simplex:e name="ne">e</simplex:e>c</simplex:c>""") match {
            case Right(tree) => {
              assert( tree.textFragments == List("c", "c","c") )
            }
            case Left(error) => fail(error.message)
          }
        }
        "there is nothing before the first component" in {
          treeFromComponent("""<simplex:c name="nc"><simplex:d name="nd">d</simplex:d>c<simplex:e name="ne">e</simplex:e>c</simplex:c>""") match {
            case Right(tree) => assert( tree.textFragments == List("", "c","c") )
            case Left(error) => fail(error.message)
          }
        }
        "there is nothing after the last component" in {
          treeFromComponent("""<simplex:c name="nc">c<simplex:d name="nd">d</simplex:d>c<simplex:e name="ne">e</simplex:e></simplex:c>""") match {
            case Right(tree) => assert( tree.textFragments == List("c", "c","") )
            case Left(error) => fail(error.message)
          }
        }
        "there is nothing between all components" in {
          treeFromComponent("""<simplex:c name="nc"><simplex:d name="nd">d</simplex:d><simplex:e name="ne">e</simplex:e></simplex:c>""") match {
            case Right(tree) => assert( tree.textFragments == List("", "","") )
            case Left(error) => fail(error.message)
          }
        }
      }
    }

  "extractFragments" should {
    "extract all fragments" when {
      "root contains 3 components and text around them" in {
        val fragments = HtmlInternalParser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree)
        assert(fragments.size == 4)
        assert(fragments(0) == "<!DOCTYPE html>\n<html lang=\"en\" xmlns:simplex=\"http://simplexportal.com/simplex\">\n    ")
        assert(fragments(1) == "\n    <p>paragraph</p>\n    ")
        assert(fragments(2) == "xx")
        assert(fragments(3) == "\n</html>\n\n")
      }
      "inner component contains 1 component and text around it" in {
        val fragments = HtmlInternalParser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree.findByName("div1").get)
        assert(fragments.size == 2)
        assert(fragments(0) == "\n            bodyOfDiv_start\n            ")
        assert(fragments(1) == "\n            bodyOfDiv1_end\n        ")
      }
      "inner component contains 2 component, and the last one with nobody" in {
        val fragments = HtmlInternalParser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree.findByName("body1").get)
        assert(fragments.size == 3)
        assert(fragments(0) == "\n        ")
        assert(fragments(1) == "\n        ")
        assert(fragments(2) == "\n    ")
      }
      "no inner components and body" in {
        val fragments = HtmlInternalParser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree.findByName("div2").get)
        assert(fragments.size == 1)
        assert(fragments(0) == "\n                bodyOfDiv2\n            ")
      }
      "no inner components and empty body" in {
        val fragments = HtmlInternalParser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree.findByName("emptybody").get)
        assert(fragments.size == 1)
        assert(fragments(0) == "")
      }
      "no inner components and no body" in {
        val fragments = HtmlInternalParser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree.findByName("nobody").get)
        assert(fragments.size == 0)
      }
    }
  }
}
