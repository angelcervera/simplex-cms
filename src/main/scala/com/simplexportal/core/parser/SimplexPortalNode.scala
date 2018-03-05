package com.simplexportal.core.parser

case class Location(line: Long, column: Long, characterOffset: Int) {
  override def toString: String = s"[${line},${column},${characterOffset}]"
}

case class SimplexPortalNode(
  `type`: String,
  start: Location,
  end: Location,
  parameters: Map[String, String],
  children: List[SimplexPortalNode],
  templateFragments: List[String]
)

