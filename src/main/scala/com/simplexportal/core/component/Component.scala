package com.simplexportal.core.component

import com.simplexportal.core.datamodel.SimplexPortalError
import com.simplexportal.core.renderer.RenderContext

case class ComponentMetadata(
  id: String,
  `type`: String,
  fullTag: String,
  executionOrder: Long,
  renderId: String = "velocity",
  parserId: String = "html"
)

/**
  * Implementation of a component, that is going to take on ComponentMetadata and render it.
  */
trait Component {
  def render(component: ComponentMetadata, renderContext: RenderContext): Either[SimplexPortalError, String]
}
