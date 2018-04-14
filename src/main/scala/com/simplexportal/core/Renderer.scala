package com.simplexportal.core

import com.simplexportal.core.dao.{Component, Page}
import com.simplexportal.core.datamodel.ComponentDefinition

import scala.annotation.tailrec


object Renderer {

  @tailrec
  private def sandwichMerge[A](seq1: Seq[A], seq2: Seq[A], acc: Seq[A] = Seq()): Seq[A] = seq1 match {
    case Nil => acc ++ seq2
    case head :: tail => sandwichMerge(seq2, tail, acc :+ head)
  }

  def render(cmp: ComponentDefinition): String = cmp.children match {
    case Nil => cmp.templateFragments.head // Render using Velocity or Markdown
    case children => sandwichMerge(cmp.templateFragments, children.map(render)) mkString
  }

  def reconstructComponentSource(component: Component) =
    s"""<simplex:${component.metadata.`type`} name="${component.metadata.name}" ${component.metadata.parameters.map(entry=>s"""${entry._1}="${entry._2}"""").mkString(" ")}>${component.data}</simplex:${component.metadata.`type`}>"""


  // FIXME: Refactoring!!!
  def buildCustomComponentDefinition(page: Page): ComponentDefinition = {

    def merge(definition: ComponentDefinition, custom: Map[String, ComponentDefinition]): ComponentDefinition = {
      val name = definition.parameters.get("name").getOrElse("")
      custom.get(name) match {
        case None => definition.copy(children = definition.children.map(merge(_, custom)))
        case Some(custom) => custom
      }
    }

    def parse(cmp: Component): ComponentDefinition = Parser.treeNodes(reconstructComponentSource(cmp)) match {
      case Right(definition) => definition
      case Left(error) => throw new Exception(error.message)
    }

    val templateTree = Parser.treeNodes(page.template.data) match { // TODO: Cache
      case Right(tree) => tree
      case Left(error) => throw new Exception(error.message)
    }

    val componentTrees = page
      .components
      .map(cmp => cmp.metadata.name -> parse(cmp))
      .toMap

    merge(templateTree, componentTrees)

  }

  def render(page: Page): String = render(buildCustomComponentDefinition(page))

}
