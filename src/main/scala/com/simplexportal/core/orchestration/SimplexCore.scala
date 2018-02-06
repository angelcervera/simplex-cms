package com.simplexportal.core.orchestration

import com.simplexportal.core.component.Component
import com.simplexportal.core.parser.{HtmlParser, Parser, ParserError}
import com.simplexportal.core.renderer.RenderContext

object SimplexCore {

  /**
    * Extract all components in the first level, execute the component and fill with the result.
    * @param text
    * @param context
    * @return
    */
  def render(text: String, context: RenderContext): Either[ParserError, String] = ???

}

