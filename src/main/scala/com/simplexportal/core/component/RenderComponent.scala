package com.simplexportal.core.component

import com.simplexportal.core.datamodel.SimplexPortalError
import com.simplexportal.core.parser.Parser
import com.simplexportal.core.renderer.{RenderContext, Renderer}

/**
  * Basic component that execute inner components and replace the full tag with the result.
  */
object RenderComponent extends Component {

  override def render(component: ComponentMetadata, renderContext: RenderContext): Either[SimplexPortalError, String] =
  {
    for { // TODO: Apply monad transformer with cats ???
      render <- Renderer.lookup(component.renderId).right
      parser <- Parser.lookup(component.parserId).right
      nodes <- parser.searchComponents(component.fullTag).right
    } yield {

      // Render each inner component and replace it by the result.
      val finalTemplate = parser.extractBody(component) // TODO: Execute inner components

      // Render the content.
      render.render(finalTemplate, renderContext)
    }

  }
}
