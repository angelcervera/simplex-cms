package com.simplexportal.core.component

import com.simplexportal.core.datamodel.SimplexPortalError

/**
  * Basic component that only return its content.
  */
object ConstantComponent extends Component {

  override def render(component: ComponentMetadata/*, renderContext: RenderContext*/): Either[SimplexPortalError, String] = ???

}
