package com.simplexportal.core

import com.simplexportal.core.datamodel.ComponentDefinition

import scala.annotation.tailrec

object Renderer {

  @tailrec
  private def sandwichMerge[A](seq1: Seq[A], seq2: Seq[A], acc: Seq[A] = Seq()): Seq[A] = seq1 match {
    case List() => acc
    case head :: tail => sandwichMerge(seq2, tail, acc :+ head)
  }

  def render(cmp: ComponentDefinition): String = cmp.children match {
    case Nil => cmp.templateFragments.head // Render using Velocity or Markdown
    case children => sandwichMerge(cmp.templateFragments, children.map(render)) mkString
  }

}
