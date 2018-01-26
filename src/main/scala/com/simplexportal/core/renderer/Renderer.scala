package com.simplexportal.core.renderer

case class RenderContext()

trait Renderer {

  /**
    * It's going to render the final fragment.
    * That means that in this moment, there is not any component in the template to render.
    */
  def render(renderContext: RenderContext, template: String): String

}
