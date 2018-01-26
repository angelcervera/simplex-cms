package com.simplexportal.core.orchestration

import com.simplexportal.core.parser.{HtmlParser, Parser, ParserError}
import com.simplexportal.core.renderer.RenderContext

object SimplexCore {

  val parser: Parser = HtmlParser

  def render(text: String, context: RenderContext): Either[ParserError, String] = ??? /*parser.searchComponents(text, 1) match {
    case Right(nodeLocations) => {
      nodeLocations.map(loc => parser.extractBody())
    }
    case Left(error) => Left(error)
  }*/

}

