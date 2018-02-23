package com.simplexportal.core.component

import com.simplexportal.core.datamodel.SimplexPortalError

/**
  * Basic component that execute inner components and replace the full tag with the result.
  */
object RenderComponent extends Component {

  override def render(component: ComponentMetadata/*, renderContext: RenderContext*/): Either[SimplexPortalError, String] = ???
//  {
//
//
//    Parser.searchComponents(component.fullTag, 2) match {
//      case Right(lst) => VelocityRenderer.render( "" , lst.map(_.name).zip(processInnerComponents(lst)).toMap)
//      case _ => _
//    }
//
//    for { // TODO: Apply monad transformer with cats ???
//      nodes <- .right
//    } yield {
//      val renderedContents = nodes.map(_.id).zip(processInnerComponents(nodes)).toMap
//
//      // Render the content.
//      val body = parser.extractBody(component)
//      renderer.render(body, renderContext)
//    }
//
//
//
//  }

  // TODO: Generalize because will be used by all components, except for the constant one.
  def processInnerComponents(innerComponents: List[ComponentMetadata]): List[Either[SimplexPortalError,String]] = ???
//    innerComponents.map(cmpMetadata => for {
//        renderer <- Renderer.lookup(cmpMetadata.`type`).right
//        inner <- renderer.render(cmpMetadata.fullTag, renderContext).right
//      } yield inner
//    )

//    for {
//      cmpMetadata <- nodes
//      renderer <- Renderer.lookup(cmpMetadata.`type`).right
//      inner <- renderer.render(cmpMetadata.fullTag, RenderContext()).right
//    } yield inner




}
