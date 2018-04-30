package com.simplexportal.core.renderer

import com.simplexportal.core.parser._

import scala.annotation.tailrec

/**
  * Generate Html from a page.
  */
object Renderer {

  @tailrec
  private def sandwichMerge[A](seq1: Seq[A], seq2: Seq[A], acc: Seq[A] = Seq()): Seq[A] = seq1 match {
    case Nil => acc ++ seq2
    case head :: tail => sandwichMerge(seq2, tail, acc :+ head)
  }

  private def applyTransformers(node: ASTModel): String = node.textFragments.head

  def render(node: ASTModel): String = node.children match {
    case Nil => applyTransformers(node)
    case children => sandwichMerge(node.textFragments, children.map(render)) mkString
  }

}
