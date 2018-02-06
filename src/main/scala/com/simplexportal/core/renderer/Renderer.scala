package com.simplexportal.core.renderer

import com.simplexportal.core.datamodel.SimplexPortalError

import scala.concurrent.Future

case class RenderContext()

sealed trait RendererError extends SimplexPortalError

case class RendererNotFound(id: String) extends RendererError {
  override def toString: String = s"$id not found in the list of registered renderers."
}

trait Renderer {

  /**
    * It's going to render the final fragment.
    * That means that in this moment, there is not any component in the template to render.
    */
  def render(template: String, renderContext: RenderContext): String

}

object Renderer {

  /**
    * List of implemented renderers.
    * TODO: Load dynamically checking the classpath
    */
  val renderers: Map[String, Renderer] = Map("velocity" -> new VelocityRenderer())

  /**
    * Look up a renderer implementation.
    */
  def lookup(id:String): Either[SimplexPortalError, Renderer] =
    renderers.get(id) match {
      case Some(renderer) => Right(renderer)
      case None => Left(RendererNotFound(id))
    }


}
