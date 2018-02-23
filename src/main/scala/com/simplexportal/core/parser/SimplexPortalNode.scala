package com.simplexportal.core.parser

import java.io.StringReader
import javax.xml.stream
import javax.xml.stream.{XMLInputFactory, XMLStreamException, XMLStreamReader}
import javax.xml.stream.XMLStreamConstants._

import com.ctc.wstx.api.WstxInputProperties
import com.ctc.wstx.stax.WstxInputFactory
import com.simplexportal.core.Configuration
import org.codehaus.stax2.XMLInputFactory2

import scala.annotation.tailrec

case class Location(line: Long, column: Long, characterOffset: Int) {
  override def toString: String = s"[${line},${column},${characterOffset}]"
}

case class SimplexPortalNode(
  `type`: String,
  start: Location,
  end: Option[Location] = None,
  parameters: Map[String, String] = Map.empty,
  children: List[SimplexPortalNode] = List.empty,
  fullTag: Option[String] = None
) {

  def isOpen = end.isEmpty
  def isClosed = end.isDefined

  /**
    * Add a direct child.
    *
    * @param child
    * @return
    */
  def addChild(child: SimplexPortalNode): SimplexPortalNode = this.copy(children = children :+ child)
  
  def updateLastChild(child: SimplexPortalNode): SimplexPortalNode = this.copy(children = children.init :+ child )

  def stringAsTree(deep:Int = 0): Any =
    s"${" "*deep}${`type`}, from ${start} to ${end} with parameters ${parameters.map(p=>p._1+"="+p._2).mkString("[",",","]")}" + children.map(_.stringAsTree(deep+4)).mkString("\n","\n","")

}

object SimplexPortalNode {

  implicit class XmlStreamLocationEnricher(location: stream.Location) {
    implicit def htmlParserLocation = Location(location.getLineNumber, location.getColumnNumber, location.getCharacterOffset)
  }

  implicit class ParserEnricher(parser: XMLStreamReader) {

    def buildUncompletedNode = SimplexPortalNode(
      extractLocalName,
      parser.getLocation.htmlParserLocation,
      None,
      (0 to parser.getAttributeCount-1).map(idx=>parser.getAttributeName(idx).getLocalPart->parser.getAttributeValue(idx)).toMap
    )

    def completeNode(node: SimplexPortalNode): SimplexPortalNode = node.copy(
      end = Some(parser.getLocation.htmlParserLocation)
    )

    def extractLocalName = parser.getLocalName.split(":") match {
      case parts if parts.size == 1 => parts(0)
      case parts if parts.size == 2 => parts(1)
      case _ => throw new Exception(s"xml format error. [${parser.getLocalName}] does not follow the XML format of namespace:tagname")
    }

    def isNodeToExtract(tags: Seq[String], ns: Seq[String]): Boolean = isNodeToExtract(parser.getLocalName, tags, ns)
    def isNodeToExtract(): Boolean = isNodeToExtract(tags, namespaces)

    def isNodeToExtract(fullTagName: String, tags: Seq[String], ns: Seq[String]): Boolean = fullTagName.split(":") match {
      case parts if parts.size == 1 => tags.contains(parts(0))
      case parts if parts.size == 2 => tags.contains(parts(1)) || namespaces.contains(parts(0))
      case _ => throw new Exception(s"xml format error. [${fullTagName}] does not follow the XML format of namespace:tagname")
    }
  }

  // Simplex tags
  val SIMPLEX_NS_URI = Configuration.config.getString("simplex.core.parsers.html.namespace.uri") // "http://simplexportal.com/simplex")
  val SIMPLEX_NS_PREFIX = Configuration.config.getString("simplex.core.parsers.html.namespace.prefix") // "simplex"

  // html tags
  val HTML_NS_URI = "http://www.w3.org/1999/xhtml"
  val HTML_NS_PREFIX = "html"

  val tags: Seq[String] = Seq(SIMPLEX_NS_PREFIX)
  val namespaces: Seq[String] = Seq(SIMPLEX_NS_PREFIX)


  case class UnBalancedTree(message: String, exceptedEndTag: SimplexPortalNode, foundTag: Option[SimplexPortalNode] = None) extends ParserError
  object UnBalancedTree extends ParserError {
    def apply(exceptedEndTag: SimplexPortalNode, foundTagEndName: String, foundTagEndLocation: stream.Location) =
      new UnBalancedTree(
        s"Expected the end of [${exceptedEndTag}], but found the end of a [${foundTagEndName}] at ${foundTagEndLocation.htmlParserLocation}",
        exceptedEndTag,
        Some(SimplexPortalNode(foundTagEndName, foundTagEndLocation.htmlParserLocation, None, Map.empty)))
  }

  case class UnCompleteTree(message: String = "UnComplete tree") extends ParserError

  case class UnHandledXMLStreamError(exception: XMLStreamException) extends ParserError

  case class UnHandledException(exception: Throwable) extends ParserError

  private val xmlInputFactory = {
    val tmp = new WstxInputFactory

    tmp.configureForRoundTripping()

    // http://woodstox.codehaus.org/javadoc/stax-api/1.0/javax/xml/stream/XMLInputFactory.html
    // http://woodstox.codehaus.org/4.2.0/javadoc/com/ctc/wstx/api/WstxInputProperties.html

    tmp.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
    tmp.setProperty(XMLInputFactory.IS_VALIDATING, false)
    tmp.setProperty(XMLInputFactory.SUPPORT_DTD, false)
    tmp.setProperty(XMLInputFactory2.P_PRESERVE_LOCATION, true)
    tmp.setProperty(WstxInputProperties.P_CACHE_DTDS, true)
    tmp.setProperty(WstxInputProperties.P_CACHE_DTDS_BY_PUBLIC_ID, true)
    tmp.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false)

    //		xmlInputFactory.configureForSpeed();

    // http://woodstox.codehaus.org/2.0.6/javadoc/org/codehaus/stax2/XMLInputFactory2.html#configureForRoundTripping%28%29
    //		tmp.setProperty(XMLInputFactory2.IS_COALESCING,Boolean.FALSE);
    //		tmp.setProperty(XMLInputFactory2.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
    //		tmp.setProperty(XMLInputFactory2.P_REPORT_ALL_TEXT_AS_CHARACTERS, Boolean.FALSE);
    //		tmp.setProperty(XMLInputFactory2.P_REPORT_PROLOG_WHITESPACE, Boolean.TRUE);

    tmp
  }

  def eventInfo(xmlReader: XMLStreamReader): String = xmlReader.getEventType match {
    case START_ELEMENT => "START_ELEMENT"
    case END_ELEMENT => "END_ELEMENT"
    case START_DOCUMENT => "START_DOCUMENT"
    case END_DOCUMENT => "END_DOCUMENT"
    case x => x.toString
  }


  /**
    * Return a list of ordered nodes from a template.
    */
  def treeNodes(template:String) :  Either[ParserError, SimplexPortalNode] = {
    val reader = new StringReader(template)
    try {
      val xml = xmlInputFactory.createXMLStreamReader(reader)

      @tailrec
      def next(stack: List[SimplexPortalNode] = List.empty): Either[ParserError, SimplexPortalNode] = xml.hasNext match {
        case true => xml.next match {
          case START_ELEMENT if xml.isNodeToExtract => next(xml.buildUncompletedNode :: stack)
          case END_ELEMENT if xml.isNodeToExtract => stack match {
            case endedNode :: tail if endedNode.`type` == xml.extractLocalName => tail match {
              case Nil => Left(UnBalancedTree)
              case parent :: tail => next(parent.addChild(xml.completeNode(endedNode)) :: tail)
            }
            case endedNode :: _ => Left(UnBalancedTree(endedNode, xml.extractLocalName, xml.getLocation))
          }
          case END_DOCUMENT => stack match {
            case root :: Nil => Right( xml.completeNode(root) )
            case _ => Left(UnCompleteTree(s"Expected only root open, but found ${stack.size} elements open"))
          }
          case _ => next(stack)
        }
        case false if !stack.isEmpty => Left(UnCompleteTree("Arrived to the end but the nodes are still open."))
        case false => Left(UnCompleteTree("End of the document does not found."))
      }


      next(List(SimplexPortalNode("root", xml.getLocation.htmlParserLocation )))
    } catch {
      case ex: XMLStreamException => Left(UnHandledXMLStreamError(ex))
      case ex: Throwable => Left(UnHandledException(ex))
    } finally {
      reader.close()
    }
  }

}
