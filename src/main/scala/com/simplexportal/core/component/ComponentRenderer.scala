package com.simplexportal.core.component

import com.simplexportal.core.datamodel.ComponentDefinition

trait Component {
  val name: String
  val definition: ComponentDefinition
  def render(content: String): String
}

case class RawContent(definition: ComponentDefinition) extends Component {
  override val name: String = "content"
  override def render(content: String): String = content
}

object RawContent {

}
