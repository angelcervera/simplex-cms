package com.simplexportal.core

import com.simplexportal.core.datamodel.ComponentDefinition

import scala.annotation.tailrec


// TODO: In this first iteration, check if there is a custom content for the component, and use it directly.
// TODO: Next step, parser the custom component to find new inner components.
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

}
