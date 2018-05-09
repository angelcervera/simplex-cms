package com.simplexportal.core.renderer

import com.simplexportal.tools.BuildStaticSite.logger
import com.simplexportal.core.dao.{ComponentMetadata, Metadata}
import com.simplexportal.core.datamodel.SimplexPortalError
import com.simplexportal.core.parser._
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec

/**
  * Generate Html from a page.
  */
object Renderer extends LazyLogging {

  // TODO: Load it dynamically, maybe using http://software.clapper.org/classutil/
  val transformers = Seq(MarkdownTransformer).map(t => t.name -> t).toMap

  @tailrec
  private def sandwichMerge[A](seq1: Seq[A], seq2: Seq[A], acc: Seq[A] = Seq()): Seq[A] = seq1 match {
    case Nil => acc ++ seq2
    case head :: tail => sandwichMerge(seq2, tail, acc :+ head)
  }

  def applyTransformers(txt :String, metadata: ComponentMetadata): Either[SimplexPortalError, String] =
    applyTransformers(txt, metadata, metadata.transformers.flatMap(transformers.get(_)))

  def applyTransformers(txt :String, metadata: ComponentMetadata, transformers :Seq[Transformer]): Either[SimplexPortalError, String] =
    transformers match {
      case Seq() => Right(txt)
      case hd +: tl => hd.transform(metadata, txt).fold(Left(_), applyTransformers(_, metadata, tl))
    }

  def render(node: ASTModel): String = node.children match {
    case Nil => node.metadata match {
      case m: ComponentMetadata => applyTransformers(node.textFragments.head, m) fold(e=>{
        logger.error(s"Error [${e.message}] in component ${node.metadata.path}")
        ""
      }, html=>html)
      case m: Metadata => node.textFragments.head
    }
    case children => sandwichMerge(node.textFragments, children.map(render)) mkString
  }

}
