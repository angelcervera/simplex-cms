package com.simplexportal.core.parser

import com.simplexportal.core.component.ComponentMetadata
import com.simplexportal.core.datamodel.SimplexPortalError

import scala.concurrent.Future

case class Location(line: Long, column: Long, characterOffset: Int) {
  override def toString: String = s"[${line},${column},${characterOffset}]"
}

case class NodeLocation(
  `type`: String,
  start: Location,
  end: Option[Location],
  deep: Long,
  parameters: Map[String, Any]
) {
  def name: Option[String] = parameters.get("name").map(_.toString)
}

trait ParserError extends SimplexPortalError

case class ParserNotFound(id: String) extends ParserError {
  override def toString: String = s"$id not found in the list of registered parsers."
}




trait Parser {

  def searchComponents(text: String, maxLevel: Int): Either[SimplexPortalError, List[NodeLocation]]

  def searchComponents(text: String): Either[SimplexPortalError, List[NodeLocation]]

  def extractBody(text: String, nodeLocation: NodeLocation): String

  def extractBody(cmpMetadata: ComponentMetadata): String

}

object Parser {

  // TODO: Load dynamically checking the classpath
  val parsers: Map[String, Parser] = Map("html" -> HtmlParser)

  def lookup(id:String): Either[SimplexPortalError, Parser] =
    parsers.get(id) match {
      case Some(parser) => Right(parser)
      case None => Left(ParserNotFound(id))
    }

}
