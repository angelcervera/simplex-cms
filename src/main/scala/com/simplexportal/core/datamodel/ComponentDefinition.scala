package com.simplexportal.core.datamodel

case class Location(line: Long, column: Long, characterOffset: Int) {
  override def toString: String = s"[${line},${column},${characterOffset}]"
}

// TODO: Clean up. Are start and end necessary?
case class ComponentDefinition(
  `type`: String,
  start: Location,
  end: Location,
  parameters: Map[String, String],
  children: List[ComponentDefinition],
  templateFragments: List[String]
)

