package com.simplexportal.core.renderer

import com.simplexportal.core.Configuration
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.tools.ToolManager
import com.simplexportal.core.util._

object Velocity {
  def apply: Velocity = new Velocity()
}

class Velocity extends Renderer {

  lazy val velocityEngine: VelocityEngine = new VelocityEngine(Configuration.config.toProperties("cms.velocity.engine"))

  lazy val velocityToolManager: ToolManager = new ToolManager

  override def render(renderContext: RenderContext, template: String): String = {


    ""
  }

}
