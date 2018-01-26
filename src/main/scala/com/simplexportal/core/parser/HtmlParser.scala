package com.simplexportal.core.parser

import java.io.StringReader
import javax.xml.stream
import javax.xml.stream.{XMLInputFactory, XMLStreamConstants, XMLStreamException, XMLStreamReader}

import com.ctc.wstx.api.WstxInputProperties
import com.ctc.wstx.stax.WstxInputFactory
import com.simplexportal.core.Configuration
import org.codehaus.stax2.XMLInputFactory2

import scala.annotation.tailrec

import scala.collection.JavaConverters._

object HtmlParser extends Parser {

  implicit class XmlStreamLocationEnricher(location: stream.Location) {
    implicit def htmlParserLocation = Location(location.getLineNumber, location.getColumnNumber, location.getCharacterOffset)
  }

    // Simplex tags
  val SIMPLEX_NS_URI = Configuration.config.getString("simplex.core.parsers.html.namespace.uri") // "http://simplexportal.com/simplex")
  val SIMPLEX_NS_PREFIX = Configuration.config.getString("simplex.core.parsers.html.namespace.prefix") // "simplex"

  // html tags
  val HTML_NS_URI = "http://www.w3.org/1999/xhtml"
  val HTML_NS_PREFIX = "html"

  val tags: Seq[String] = Seq(SIMPLEX_NS_PREFIX)
  val namespaces: Seq[String] = Seq(SIMPLEX_NS_PREFIX)


  object UnBalancedTree {
    def apply(exceptedEndTag: NodeLocation, foundTagEndName: String, foundTagEndLocation: stream.Location) =
      new UnBalancedTree(
        s"Expected the end of [${exceptedEndTag}], but found the end of a [${foundTagEndName}] at ${foundTagEndLocation.htmlParserLocation}",
        exceptedEndTag,
        Some(NodeLocation(foundTagEndName, foundTagEndLocation.htmlParserLocation, None, 0L, Map.empty)))
  }

  case object UnCompleteTree extends ParserError

  case class UnBalancedTree(message: String, exceptedEndTag: NodeLocation, foundTag: Option[NodeLocation] = None) extends ParserError

  case class UnHandledXMLStreamError(exception: XMLStreamException) extends ParserError

  case class UnHandledException(exception: Throwable) extends ParserError

  private val xmlInputFactory = {
    val tmp = new WstxInputFactory
    // http://woodstox.codehaus.org/javadoc/stax-api/1.0/javax/xml/stream/XMLInputFactory.html
    // http://woodstox.codehaus.org/4.2.0/javadoc/com/ctc/wstx/api/WstxInputProperties.html

    tmp.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    tmp.setProperty(XMLInputFactory.IS_VALIDATING, false);
    tmp.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    tmp.setProperty(XMLInputFactory2.P_PRESERVE_LOCATION, true);
    tmp.setProperty(WstxInputProperties.P_CACHE_DTDS, true);
    tmp.setProperty(WstxInputProperties.P_CACHE_DTDS_BY_PUBLIC_ID, true);
    //		xmlInputFactory.configureForSpeed();

    // http://woodstox.codehaus.org/2.0.6/javadoc/org/codehaus/stax2/XMLInputFactory2.html#configureForRoundTripping%28%29
    //		tmp.setProperty(XMLInputFactory2.IS_COALESCING,Boolean.FALSE);
    //		tmp.setProperty(XMLInputFactory2.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
    //		tmp.setProperty(XMLInputFactory2.P_REPORT_ALL_TEXT_AS_CHARACTERS, Boolean.FALSE);
    //		tmp.setProperty(XMLInputFactory2.P_REPORT_PROLOG_WHITESPACE, Boolean.TRUE);

    tmp.configureForRoundTripping();

    tmp
  }

  override def searchComponents(text: String, maxLevel: Int): Either[ParserError, List[NodeLocation]] =
    searchComponents(text).fold(Left(_), locations => Right(locations.filter(_.deep <= maxLevel)))

  override def searchComponents(text: String): Either[ParserError, List[NodeLocation]] = {

    val reader = new StringReader(text)
    try {

      implicit class ParserEnricher(parser: XMLStreamReader) {
        implicit def containsTag(tags: Seq[String]): Boolean = tags.contains(parser.getLocalName)

        implicit def containsNamespace(namespaces: Seq[String]): Boolean = namespaces.contains(parser.getPrefix)

        implicit def buildNodeLocation(deep: Int) = NodeLocation(
          parser.getLocalName,
          parser.getLocation.htmlParserLocation,
          None,
          deep,
          (0 to parser.getAttributeCount-1).map(idx=>parser.getAttributeName(idx).getLocalPart->parser.getAttributeValue(idx)).toMap
        )

        implicit def completeNodeLocation(node: NodeLocation): NodeLocation = node.copy(end = Some(parser.getLocation.htmlParserLocation))

        implicit def isNodeToExtract(tags: Seq[String], ns: Seq[String]) = parser.containsTag(tags) || parser.containsNamespace(ns)
      }

      val parser = xmlInputFactory.createXMLStreamReader(reader)

      /**
        *
        * @param stack     Using a List as a Stack structure to manage the balanced tree of tags
        * @param locations List of confirmed balanced locations
        * @return List of locations
        */
      @tailrec
      def loop(stack: List[NodeLocation], locations: List[NodeLocation]): Either[ParserError, List[NodeLocation]] = {

        parser.hasNext match {
          case true => parser.next match { // TODO: Refactoring to use parser.nextTag to jump directly to the next tag.
            case XMLStreamConstants.START_ELEMENT if parser.isNodeToExtract(tags, namespaces) => loop(parser.buildNodeLocation(stack.size + 1) :: stack, locations)
            case XMLStreamConstants.END_ELEMENT if parser.isNodeToExtract(tags, namespaces) => stack match {
              case Nil => Left(UnCompleteTree)
              case head :: tail if head.`type` != parser.getLocalName => Left(UnBalancedTree(head, parser.getLocalName, parser.getLocation))
              case head :: tail => loop(tail, parser.completeNodeLocation(head) :: locations)
            }
            case _ => loop(stack, locations)
          }

          case false => stack match {
            case Nil => Right(locations)
            case _ => Left(UnBalancedTree("No all managed tags are closed", stack.head))
          }
        }
      }

      loop(Nil, Nil)

    } catch {
      case ex: XMLStreamException => Left(UnHandledXMLStreamError(ex))
      case ex: Throwable => Left(UnHandledException(ex))
    } finally {
      reader.close()
    }
  }

  override def extractBody(text: String, nodeLocation: NodeLocation) = {
    require(nodeLocation.end.isDefined, "End location must be defined")
    if(nodeLocation.start.characterOffset == nodeLocation.end.get.characterOffset)
      ""
    else
      text.substring(text.indexOf('>', nodeLocation.start.characterOffset) +1, nodeLocation.end.get.characterOffset)
  }

}
