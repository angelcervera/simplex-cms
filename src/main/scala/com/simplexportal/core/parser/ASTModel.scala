package com.simplexportal.core.parser

import com.simplexportal.core.dao._

sealed trait ASTModel {
  val metadata: Metadata
  val children: List[ComponentNode]
  val textFragments: List[String]
}

case class PageNode(
  metadata: PageMetadata,
  children: List[ComponentNode],
  textFragments: List[String]
) extends ASTModel

case class ComponentNode(
  metadata: ComponentMetadata,
  children: List[ComponentNode],
  textFragments: List[String]
) extends ASTModel
