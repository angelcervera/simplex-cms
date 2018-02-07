package com.simplexportal.core.component

import com.simplexportal.core.datamodel.SimplexPortalError
import com.simplexportal.core.parser.Parser
import com.simplexportal.core.renderer.RenderContext

/**
  * Basic component that only return its content.
  */
object ConstantComponent extends Component {

  override def render(component: ComponentMetadata, renderContext: RenderContext): Either[SimplexPortalError, String] =
    for {
      parser <- Parser.lookup(component.parserId).right
    } yield parser.extractBody(component)

}
