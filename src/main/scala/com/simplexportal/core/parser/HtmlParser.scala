package com.simplexportal.core.parser

import java.io.Reader
import javax.xml.stream
import javax.xml.stream.{ XMLInputFactory, XMLStreamConstants, XMLStreamException, XMLStreamReader }

import com.ctc.wstx.api.WstxInputProperties
import com.ctc.wstx.stax.WstxInputFactory
import org.codehaus.stax2.XMLInputFactory2

import scala.annotation.tailrec

object HtmlParser {

  implicit class XmlStreamLocationEnricher(location: stream.Location) {
    implicit def htmlParserLocation = Location(location.getLineNumber, location.getColumnNumber, location.getCharacterOffset)
  }

  // Simplex tags
  val SIMPLEX_NS_URI = "http://simplexportal.com/simplex"
  val SIMPLEX_NS_PREFIX = "simplex"

  // html tags
  val HTML_NS_URI = "http://www.w3.org/1999/xhtml"
  val HTML_NS_PREFIX = "html"

  // No closed tags
  val noClosedTags = Seq("img")

  case class Location(line: Long, column: Long, characterOffset: Int) {
    override def toString: String = s"[${line},${column},${characterOffset}]"
  }

  case class NodeLocation(`type`: String, start: Location, end: Option[Location], deep: Long)

  sealed trait ParserError

  object UnBalancedTree {
    def apply(exceptedEndTag: NodeLocation, foundTagEndName: String, foundTagEndLocation: stream.Location) =
      new UnBalancedTree(
        s"Expected the end of [${exceptedEndTag}], but found the end of a [${foundTagEndName}] at ${foundTagEndLocation.htmlParserLocation}",
        exceptedEndTag,
        Some(NodeLocation(foundTagEndName, foundTagEndLocation.htmlParserLocation, None, 0L)))
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

  def searchTags(html: Reader, tags: Seq[String], ns: Seq[String], maxLevel: Int): Either[ParserError, List[NodeLocation]] =
    searchTags(html, tags, ns).fold(Left(_), locations => Right(locations.filter(_.deep <= maxLevel)))

  def searchTags(html: Reader, tags: Seq[String], ns: Seq[String]): Either[ParserError, List[NodeLocation]] =

    try {

      implicit class ParserEnricher(parser: XMLStreamReader) {
        implicit def containsTag(tags: Seq[String]): Boolean = tags.contains(parser.getLocalName)
        implicit def containsNamespace(namespaces: Seq[String]): Boolean = namespaces.contains(parser.getPrefix)
        implicit def isNodeToExtract(tags: Seq[String], ns: Seq[String]) = parser.containsTag(tags) || parser.containsNamespace(ns)
      }

      val parser = xmlInputFactory.createXMLStreamReader(html)

      /**
       *
       * @param stack     Using a List as a Stack structure to manage the balanced tree of tags
       * @param locations List of confirmed balanced locations
       * @return List of locations
       */
      @tailrec
      def loop(stack: List[NodeLocation], locations: List[NodeLocation]): Either[ParserError, List[NodeLocation]] = {

        parser.hasNext match {
          case true => parser.next match {
            case XMLStreamConstants.START_ELEMENT if parser.isNodeToExtract(tags, ns) =>
              loop(
                NodeLocation(
                  parser.getLocalName,
                  parser.getLocation.htmlParserLocation,
                  None,
                  stack.size + 1) :: stack,
                locations)
            case XMLStreamConstants.END_ELEMENT if parser.isNodeToExtract(tags, ns) => stack match {
              case Nil => Left(UnCompleteTree)
              case head :: tail if head.`type` != parser.getLocalName => Left(UnBalancedTree(head, parser.getLocalName, parser.getLocation))
              case head :: tail => loop(tail, head.copy(end = Some(parser.getLocation.htmlParserLocation)) :: locations)
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
    }

}
