package com.simplexportal.core.renderer

import com.simplexportal.core.dao.Metadata
import com.simplexportal.core.datamodel.{GenericSimplexPortalError, SimplexPortalError}
import com.vladsch.flexmark.util.options.MutableDataSet
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser

import scala.util.Try

object MarkdownTransformer extends Transformer {

  override def name: String = "markdown"

  val options = new MutableDataSet

  // uncomment to set optional extensions
  //options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));

  // uncomment to convert soft-breaks to hard breaks
  //options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

  val parser: Parser = Parser.builder(options).build
  val renderer: HtmlRenderer = HtmlRenderer.builder(options).build

  override def transform(metadata: Metadata, txt: String): Either[SimplexPortalError, String] =
    Try(renderer.render(parser.parse(txt))) fold( e => Left(GenericSimplexPortalError(e)), html => Right(html) )

}
