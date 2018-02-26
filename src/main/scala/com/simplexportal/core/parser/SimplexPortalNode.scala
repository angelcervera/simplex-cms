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
) {

  def stringAsTree(deep:Int = 0): String =
    s"${" "*deep}${`type`}, from ${start} to ${end} with parameters ${parameters.map(p=>p._1+"="+p._2).mkString("[",",","]")}" + children.map(_.stringAsTree(deep+4)).mkString("\n","\n","")
}

