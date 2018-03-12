package com.simplexportal.core.component

import com.simplexportal.core.datamodel.Error

case class ComponentMetadata(

  // One of the registered components types.
  `type`: String,

  // Unique name in this level of components.
  name: String,

  fullTag: String,

  // Offset location in the parent
  locationOffset: Long,

  executionOrder: Long,

  // List of parameters, including name.
  parameters: Map[String, Any],

  children: Set[ComponentMetadata]

)


trait ComponentError extends Error

case class ComponentNotFound(id: String) extends ComponentError {
  override def toString: String = s"$id not found in the list of registered components."
}







/**
  * Implementation of a component, that is going to take on ComponentMetadata and render it.
  */
trait Component {
  def render(component: ComponentMetadata/*, renderContext: RenderContext*/): Either[Error, String]
}

object Component {

  // TODO: Load dynamically checking the classpath
  val components: Map[String, Component] = Map(
    "cnt" -> ConstantComponent,
    "render" -> RenderComponent
  )

  def lookup(name: String): Either[Error, Component] =
    components.get(name) match {
      case Some(parser) => Right(parser)
      case None => Left(ComponentNotFound(name))
    }
}
