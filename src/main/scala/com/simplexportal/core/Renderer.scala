package com.simplexportal.core

import com.simplexportal.core.dao.Page
import com.simplexportal.core.parser.{ComponentDefinition, Parser}

import scala.annotation.tailrec

/**
  * Generate Html from a page.
  */
object Renderer {

  def render(page: Page): String = render(Parser.treeNodes(page))

  @tailrec
  private def sandwichMerge[A](seq1: Seq[A], seq2: Seq[A], acc: Seq[A] = Seq()): Seq[A] = seq1 match {
    case Nil => acc ++ seq2
    case head :: tail => sandwichMerge(seq2, tail, acc :+ head)
  }


  // TODO: Add content into the Component and also pass the Page. So we can isolate from parser (so don't share ComponentDefinition)
  private def applyTransformers(cmp: ComponentDefinition, content: String): String = content

  // TODO: update unit test to avoid expose the function
  private[core] def render(cmp: ComponentDefinition): String = cmp.children match {
    case Nil => applyTransformers(cmp, cmp.templateFragments.head)
    case children => sandwichMerge(cmp.templateFragments, children.map(render)) mkString
  }

}
