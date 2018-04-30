package com.simplexportal.core.parser

import com.simplexportal.core.dao.DataModel.{ComponentMetadata, PageMetadata, TemplateMetadata}
import com.simplexportal.core.dao.Storage


sealed trait ParserError extends Error {
  val message: String
}

case class UnBalancedTree(message: String) extends ParserError

case class UnCompleteTree(message: String = "UnComplete tree") extends ParserError

case class UnHandledException(message: String, exception: Throwable) extends ParserError

object UnHandledException {
  def apply(exception: Throwable): UnHandledException = new UnHandledException(exception.getMessage, exception)
}


/**
  * Utilities to parse HTML ( template or other html like a content) and generate a ComponentDefinition tree.
  */
class Parser(storage: Storage) {

  val templates: Map[String, TemplateMetadata] = storage.collectTemplateMetadata.map(t => t.path -> t).toMap

  implicit class EnrichComponentMetadata(cmpMeta: ComponentMetadata) {
    def data: String = storage.readComponentData(cmpMeta)

    def reconstructComponentSource =
      s"""<simplex:${cmpMeta.`type`} name="${cmpMeta.name}" transformers="${cmpMeta.transformers.mkString(",")}" orderExecution="${cmpMeta.orderExecution}" ${cmpMeta.parameters.map(entry=>s"""${entry._1}="${entry._2}"""").mkString(" ")}>${cmpMeta.data}</simplex:${cmpMeta.`type`}>"""

    def parse: ComponentNode = HtmlInternalParser.treeFromComponent(reconstructComponentSource) match {
      case Right(definition) => definition
      case Left(error) => throw new Exception(error.message)
    }
  }

  implicit class EnrichTemplateMetadata(templateMetadata: TemplateMetadata) {
    def data: String = storage.readTemplateData(templateMetadata)
  }

  // FIXME: Refactoring!!!
  def treeNodes(page: PageMetadata): PageNode = {

    def merge(pageMeta: PageNode, customized: Map[String, ComponentNode]): PageNode = {
      def mergeComponent(cmpMeta: ComponentNode, customized: Map[String, ComponentNode]): ComponentNode = {
        customized.get(cmpMeta.metadata.name) match {
          case None => cmpMeta.copy(children = cmpMeta.children.map(mergeComponent(_, customized)))
          case Some(custom) => custom
        }
      }
      pageMeta.copy(children = pageMeta.children.map(mergeComponent(_, customized)))
    }

    val templateTree: PageNode =
      HtmlInternalParser.treeFromTemplate(templates.getOrElse(page.template, throw new Exception(s"Template ${page.template} not found.")).data) match { // TODO: Cache
        case Right(tree) => tree
        case Left(error) => throw new Exception(error.message)
      }

    val componentTrees = storage
      .collectComponentMetadata(page)
      .map(cmp => cmp.name -> cmp.parse)
      .toMap

    merge(templateTree, componentTrees)
  }
}
