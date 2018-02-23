package com.simplexportal.core.parser

import java.io.StringReader
import javax.xml.stream
import javax.xml.stream.{XMLInputFactory, XMLStreamConstants, XMLStreamException, XMLStreamReader}

import com.ctc.wstx.api.WstxInputProperties
import com.ctc.wstx.stax.WstxInputFactory
import com.simplexportal.core.Configuration
import com.simplexportal.core.component.ComponentMetadata
import com.simplexportal.core.datamodel.SimplexPortalError
import org.codehaus.stax2.XMLInputFactory2

import scala.annotation.tailrec



trait ParserError extends SimplexPortalError

/**
  * Utilities to generate a parse an html file with simplex tags and generate a tree of components.
  */
object Parser {




  def tree(in:String): Either[SimplexPortalError, ComponentMetadata] =  ???









//
//  /**
//    * Generate a sequence of nodes in the same order that their appear in the template.
//    */
//  def listOfNodes(s: String): Either[ParserError, Seq[NodeLocation]] = {
//
//    val reader = new StringReader(s)
//    try {
//      val parser = xmlInputFactory.createXMLStreamReader(reader)
//
//      /**
//        *
//        * @param stack     Using a List as a Stack structure to manage the balanced tree of tags
//        * @param locations List of confirmed balanced locations
//        * @return List of locations
//        */
//      @tailrec
//      def loop(stack: List[NodeLocation], locations: List[NodeLocation]): Either[ParserError, List[NodeLocation]] = {
//
//        parser.hasNext match {
//          case true => parser.next match { // TODO: Refactoring to use parser.nextTag to jump directly to the next tag.
//            case XMLStreamConstants.START_ELEMENT if parser.isNodeToExtract(tags, namespaces) => loop(parser.buildNodeLocation :: stack, locations)
//            case XMLStreamConstants.END_ELEMENT if parser.isNodeToExtract(tags, namespaces) => stack match {
//              case Nil => Left(UnCompleteTree)
//              case head :: tail if head.`type` != parser.extractLocalName => Left(UnBalancedTree(head, parser.extractLocalName, parser.getLocation))
//              case head :: tail => loop(tail, parser.completeNodeLocation(head) :: locations)
//            }
//            case _ => loop(stack, locations)
//          }
//
//          case false => stack match {
//            case Nil => Right(locations)
//            case _ => Left(UnBalancedTree("No all managed tags are closed", stack.head))
//          }
//        }
//      }
//
//      loop(Nil, Nil)
//
//    } catch {
//      case ex: XMLStreamException => Left(UnHandledXMLStreamError(ex))
//      case ex: Throwable => Left(UnHandledException(ex))
//    } finally {
//      reader.close()
//    }
//
//  }
//
//
//
//  def extractBody(text: String, nodeLocation: NodeLocation) = {
//    require(nodeLocation.end.isDefined, "End location must be defined")
//    if(nodeLocation.start.characterOffset == nodeLocation.end.get.characterOffset)
//      ""
//    else
//      text.substring(text.indexOf('>', nodeLocation.start.characterOffset) +1, nodeLocation.end.get.characterOffset)
//  }
//
//  def extractBody(cmpMetadata: ComponentMetadata): String = {
//    val startIdx = cmpMetadata.fullTag.indexOf(">")
//    cmpMetadata.fullTag.lastIndexOf("<") match {
//      case 0 => ""
//      case endIdx if endIdx > 0 => cmpMetadata.fullTag.substring(startIdx +1, endIdx)
//      case _ => throw new Exception("XML Format error. < char not found")
//    }
//  }

}