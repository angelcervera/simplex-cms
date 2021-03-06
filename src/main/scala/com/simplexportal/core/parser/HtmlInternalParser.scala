package com.simplexportal.core.parser

import java.io.StringReader

import com.ctc.wstx.api.WstxInputProperties
import com.ctc.wstx.stax.WstxInputFactory
import com.simplexportal.core.Configuration
import com.simplexportal.core.dao._
import javax.xml.stream
import javax.xml.stream.XMLStreamConstants.{END_DOCUMENT, END_ELEMENT, START_ELEMENT}
import javax.xml.stream.{XMLInputFactory, XMLStreamReader}
import org.codehaus.stax2.XMLInputFactory2

import scala.annotation.tailrec

private[parser] object HtmlInternalParser {

  val ATTR_TRANSFORMERS = "transformers"
  val ATTR_TRANSFORMERS_DEFAULT = "velocity"
  val ATTR_NAME = "name"
  val ATTR_ORDER_EXECUTION = "orderExecution"
  val ATTR_ORDER_EXECUTION_DEFAULT = "9999"
  val ATTRS = Seq(ATTR_NAME, ATTR_ORDER_EXECUTION, ATTR_TRANSFORMERS)

  case class Location(line: Long, column: Long, characterOffset: Int) {
    override def toString: String = s"[${line},${column},${characterOffset}]"
  }

  case class PartialNode(
    `type`: String,
    start: Location,
    end: Option[Location] = None,
    parameters: Map[String, String] = Map.empty,
    children: List[PartialNode] = List.empty
  ) {

    def isOpen = end.isEmpty
    def isClosed = end.isDefined

    def addChild(child: PartialNode): PartialNode = this.copy(children = children :+ child)

  }

  implicit class XmlStreamLocationEnricher(location: stream.Location) {
    implicit def htmlParserLocation = Location(location.getLineNumber, location.getColumnNumber, location.getCharacterOffset)
  }

  implicit class ParserEnricher(parser: XMLStreamReader) {

    def buildUncompletedNode = PartialNode(
      extractLocalName,
      parser.getLocation.htmlParserLocation,
      None,
      (0 to parser.getAttributeCount-1).map(idx=>parser.getAttributeName(idx).getLocalPart->parser.getAttributeValue(idx)).toMap
    )

    def completeNode(node: PartialNode): PartialNode = node.copy(
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

  /**
    * Return a list of ordered nodes from a template.
    */
  def buildInternalTree(template:String) :  Either[ParserError, PartialNode] = {
    val reader = new StringReader(template)
    try {
      val xml = xmlInputFactory.createXMLStreamReader(reader)

      @tailrec
      def next(stack: List[PartialNode] = List.empty): Either[ParserError, PartialNode] = xml.hasNext match {
        case true => xml.next match {
          case START_ELEMENT if xml.isNodeToExtract => next(xml.buildUncompletedNode :: stack)
          case END_ELEMENT if xml.isNodeToExtract => stack match {
            case endedNode :: tail if endedNode.`type` == xml.extractLocalName => tail match {
              case Nil => Left(UnBalancedTree(s"Expected the end of [${xml.extractLocalName}], but found nothing at ${xml.getLocation.htmlParserLocation}"))
              case parent :: tail => next(parent.addChild(xml.completeNode(endedNode)) :: tail)
            }
            case endedNode :: _ => Left(UnBalancedTree(s"Expected the end of [${endedNode}], but found the end of a [${xml.extractLocalName}] at ${xml.getLocation.htmlParserLocation}"))
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


      next(List(PartialNode("root", xml.getLocation.htmlParserLocation )))

    } catch {
      case ex: Throwable => Left(UnHandledException(ex))
    } finally {
      reader.close()
    }
  }

  def extractFragments(template: String, node: PartialNode): List[String]  =
    if(Option(node.start) == node.end)
      List.empty
    else {

      def addPart(offset: Int, start: Int, end: Int, acc: Seq[String]): (Int, Seq[String]) =
        (template.indexOf('>', end) +1, acc :+ template.substring(offset, start))

      val initialOffset = if(node.`type` == "root") 0 else template.indexOf('>', node.start.characterOffset) +1

      val (offset, acc) = node.children
        .collect { case PartialNode(_, start, Some(end), _, _) => (start.characterOffset, end.characterOffset) }
        .foldLeft((initialOffset, Seq.empty[String])) { case ((offset, acc), (start, end)) => addPart(offset, start, end, acc) }

      acc :+ template.substring(offset, node.end.get.characterOffset) toList
    }



  private def buildComponentNode(template: String, partial:PartialNode): ComponentNode = ComponentNode(
    metadata = ComponentMetadata(
      path = "",
      `type` = partial.`type`,
      name = partial.parameters.getOrElse(
        ATTR_NAME,
        throw new Exception(s"Name not found in the component [${partial.`type`}] definition on [${partial.start}] with parameters [${partial.parameters.mkString(",")}].")
      ),
      orderExecution = partial.parameters.getOrElse(ATTR_ORDER_EXECUTION, ATTR_ORDER_EXECUTION_DEFAULT).toInt,
      transformers = partial.parameters.getOrElse(ATTR_TRANSFORMERS, ATTR_TRANSFORMERS_DEFAULT).split(","),
      parameters = partial.parameters.filter { case(k,v) => !ATTRS.contains(k)}
    ),
    children = partial.children.map(buildComponentNode(template, _)),
    textFragments = extractFragments(template, partial)
  )

  /**
    * Build a tree of nodes from a template.
    * The result is always a PageNode that is wrapping the components found.
    *
    * @param template xHTML template. Important: Must be XML
    */
  def treeFromTemplate(template:String): Either[ParserError, PageNode] =
    buildInternalTree(template).right.map( root =>
      PageNode(
        metadata = PageMetadata(
          path = "",
          cache = HttpCache("1"),
          template = ""
        ),
        children = root.children.map(buildComponentNode(template, _)),
        textFragments = extractFragments(template, root)
      ))

  /**
    * Build a tree of nodes from a component body.
    * The result is always a ComponentNode.
    *
    * @param componentXML template. Important: Must be XML
    */
  def treeFromComponent(componentXML:String): Either[ParserError, ComponentNode] =
    buildInternalTree(componentXML).right.map(root => buildComponentNode(componentXML, root.children.head) )

}
