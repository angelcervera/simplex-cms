package com.simplexportal.core.parser

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

trait ParserError

trait Parser {

  def searchComponents(text: String, maxLevel: Int): Either[ParserError, List[NodeLocation]]

  def searchComponents(text: String): Either[ParserError, List[NodeLocation]]

  def extractBody(text: String, nodeLocation: NodeLocation): String

}
