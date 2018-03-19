package com.simplexportal.core

import better.files.File
import com.simplexportal.core.Parser.PartialNode
import com.simplexportal.core.datamodel.{ComponentDefinition, Location}
import org.scalatest.Matchers._
import org.scalatest.{GivenWhenThen, WordSpec}

class ParserTest extends WordSpec with GivenWhenThen {

  val Q = "\""
  implicit  class SimplexPortalNodeTestHelpers(node: ComponentDefinition) {

    implicit def cleanTemplateFragments: ComponentDefinition = node.copy(templateFragments = List.empty, children = node.children.map(_.cleanTemplateFragments))

    implicit def definition: String = {
      def location(l: Location) = s"Location(${l.line},${l.column},${l.characterOffset})"
      def parameters(params: Map[String, String]) = params.toList match {
        case Nil => "Map.empty"
        case m => m.map{case (k,v) => s"$Q$k$Q -> $Q$v$Q"} mkString ("Map(", ",",")")
      }
      def children(nodes: List[ComponentDefinition]) = nodes match {
        case Nil => "List.empty"
        case _ => nodes.map(_.definition).mkString("List(",",",")")
      }
      s"SimplexPortalNode($Q${node.`type`}$Q, ${location(node.start)}, ${location(node.end)}, ${parameters(node.parameters)}, ${children(node.children)}, List.empty)"
    }

    implicit def toPartialNode: PartialNode = PartialNode(
      `type` = node.`type`,
      start = node.start,
      end = Some(node.end),
      parameters = node.parameters,
      children = node.children.map(_.toPartialNode)
    )

    implicit def findByName(name:String): Option[ComponentDefinition] = {

      def onChildren(name: String, nodes: Seq[ComponentDefinition]): Option[ComponentDefinition] = nodes match {
        case Nil => None
        case head::tail => head.findByName(name).orElse(onChildren(name, tail))
      }

      node.parameters.get("name") match {
        case Some(n) if n==name => Some(node)
        case _ => onChildren(name, node.children)
      }

    }

  }

  def testParserResults(template: String, expected: ComponentDefinition): Unit = Parser.treeNodes(template) match {
    case Left(error) => fail(s"Threw ${error}")
    case Right(r) => assert(r == expected, s"\nExpected:\n${expected.definition}\nCurrent:\n${r.definition}")
  }

  def testParserResultsNoContent(template: String, expected: ComponentDefinition): Unit = Parser.treeNodes(template) match {
    case Left(error) => fail(s"Threw ${error}")
    case Right(r) => assert(r.cleanTemplateFragments == expected.cleanTemplateFragments, s"\nExpected:\n${expected.cleanTemplateFragments.definition}\nCurrent:\n${r.cleanTemplateFragments.definition}")
  }





  val simpleBodyDivTemplate = File("src/test/resources/com/simplexportal/core/parser/simple_body_div.html").contentAsString
  val simpleBodyDivTree =
    ComponentDefinition("root", Location(1,1,0), Location(17,1,519), Map.empty, List(
      ComponentDefinition("body", Location(3,5,86), Location(12,5,379), Map("name" -> "body1"), List(
        ComponentDefinition("div", Location(4,9,122), Location(10,9,333), Map("class" -> "bg-top","name" -> "div1"), List(
          ComponentDefinition("div", Location(6,13,203), Location(8,13,283), Map("class" -> "bg-top","name" -> "div2"), List.empty, List.empty)
        ), List.empty),
        ComponentDefinition("nobody2", Location(11,9,356), Location(11,9,356), Map.empty, List.empty, List.empty)
      ), List.empty),
      ComponentDefinition("emptybody", Location(14,5,420), Location(14,41,456), Map("name" -> "emptybody"), List.empty, List.empty),
      ComponentDefinition("nobody", Location(14,63,478), Location(14,63,478), Map("name" -> "nobody"), List.empty, List.empty)
    ), List.empty)








  val template1Template = File("src/test/resources/com/simplexportal/core/parser/template1.html").contentAsString
  val template1Tree =
    ComponentDefinition("root",Location(1,1,0),Location(182,8,9789), Map.empty, List(
      ComponentDefinition("header",Location(49,5,1837),Location(73,5,2990), Map("name"->"header1"), List(
        ComponentDefinition("component",Location(50,9,1877),Location(50,9,1877), Map("name"->"component1"), List.empty, List.empty),
        ComponentDefinition("slide",Location(63,9,2506),Location(72,7,2969), Map("id"->"slide", "name"->"slide1"), List.empty, List.empty)
      ), List.empty),
      ComponentDefinition("section",Location(75,5,3091),Location(170,5,9289), Map("id"->"content","name"->"section1"), List.empty, List.empty),
      ComponentDefinition("emptybody",Location(179,1,9716),Location(179,20,9735), Map.empty, List.empty, List.empty),
      ComponentDefinition("nobody",Location(180,1,9756),Location(180,1,9756), Map.empty, List.empty, List.empty)
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
      "respect init and the end of the template, out of components" in {
        Parser.treeNodes("<html>start<simplex:c>c<simplex:d>d</simplex:d>c<simplex:e>e</simplex:e>c</simplex:c>end</html>") match {
          case Right(tree) => {
            assert( tree.children(0).templateFragments == List("c", "c","c") )
            assert( tree.templateFragments == List("<html>start", "end</html>"))
          }
          case Left(error) => fail(error.message)
        }
      }
      "generate num. components +1 fragments" when {
        "there are content before, after and in the middle" in {
          Parser.treeNodes("<simplex:c>c<simplex:d>d</simplex:d>c<simplex:e>e</simplex:e>c</simplex:c>") match {
            case Right(tree) => assert( tree.children(0).templateFragments == List("c", "c","c") )
            case Left(error) => fail(error.message)
          }
        }
        "there is nothing before the first component" in {
          Parser.treeNodes("<simplex:c><simplex:d>d</simplex:d>c<simplex:e>e</simplex:e>c</simplex:c>") match {
            case Right(tree) => assert( tree.children(0).templateFragments == List("", "c","c") )
            case Left(error) => fail(error.message)
          }
        }
        "there is nothing after the last component" in {
          Parser.treeNodes("<simplex:c>c<simplex:d>d</simplex:d>c<simplex:e>e</simplex:e></simplex:c>") match {
            case Right(tree) => assert( tree.children(0).templateFragments == List("c", "c","") )
            case Left(error) => fail(error.message)
          }
        }
        "there is nothing between all components" in {
          Parser.treeNodes("<simplex:c><simplex:d>d</simplex:d><simplex:e>e</simplex:e></simplex:c>") match {
            case Right(tree) => assert( tree.children(0).templateFragments == List("", "","") )
            case Left(error) => fail(error.message)
          }
        }
      }
    }

  "fragments" should {
    "extract all fragments" when {
      "root contains 3 components and text around them" in {
        val fragments = Parser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree.toPartialNode)
        assert(fragments.size == 4)
        assert(fragments(0) == "<!DOCTYPE html>\n<html lang=\"en\" xmlns:simplex=\"http://simplexportal.com/simplex\">\n    ")
        assert(fragments(1) == "\n    <p>paragraph</p>\n    ")
        assert(fragments(2) == "xx")
        assert(fragments(3) == "\n</html>\n\n")
      }
      "inner component contains 1 component and text around it" in {
        val fragments = Parser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree.findByName("div1").get.toPartialNode)
        assert(fragments.size == 2)
        assert(fragments(0) == "\n            bodyOfDiv_start\n            ")
        assert(fragments(1) == "\n            bodyOfDiv1_end\n        ")
      }
      "inner component contains 2 component, and the last one with nobody" in {
        val fragments = Parser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree.findByName("body1").get.toPartialNode)
        assert(fragments.size == 3)
        assert(fragments(0) == "\n        ")
        assert(fragments(1) == "\n        ")
        assert(fragments(2) == "\n    ")
      }
      "no inner components and body" in {
        val fragments = Parser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree.findByName("div2").get.toPartialNode)
        assert(fragments.size == 1)
        assert(fragments(0) == "\n                bodyOfDiv2\n            ")
      }
      "no inner components and empty body" in {
        val fragments = Parser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree.findByName("emptybody").get.toPartialNode)
        assert(fragments.size == 1)
        assert(fragments(0) == "")
      }
      "no inner components and no body" in {
        val fragments = Parser.extractFragments(simpleBodyDivTemplate, simpleBodyDivTree.findByName("nobody").get.toPartialNode)
        assert(fragments.size == 0)
      }
    }
  }

}
