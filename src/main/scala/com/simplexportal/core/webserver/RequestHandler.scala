package com.simplexportal.core.webserver

import akka.http.javadsl.model.headers.CacheControl
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.NotModified
import akka.http.scaladsl.model.{HttpResponse, _}
import akka.http.scaladsl.model.headers.EntityTag.matchesRange
import akka.http.scaladsl.model.headers.{ETag, EntityTag, `If-None-Match`}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.Materializer
import akka.util.CompactByteString
import better.files.File
import com.simplexportal.core._
import com.simplexportal.core.dao._
import com.simplexportal.core.dao.FileSystemStorage
import com.simplexportal.core.parser.Parser
import com.simplexportal.core.renderer.Renderer
import com.typesafe.scalalogging.LazyLogging

import scala.collection.immutable

trait RequestHandler extends LazyLogging {

  implicit val materializer: Materializer

  val storage = new FileSystemStorage(Configuration.storage)
  val parser = new Parser(storage)

  val templatesMetadata = storage.collectTemplateMetadata.map(t => t.path -> t).toMap
  val paths =
    storage.collectPageMetadata.filter(p => {
      if(templatesMetadata.get(p.template).isDefined) true else {
        logger.error(s"Ignoring page [${p.path}] because is using  the template [${p.template}] and it is not defined.")
        false
      }
    }).map(p=>p.path -> p).toMap ++
    storage.collectResourceMetadata.map(r=>r.path -> r).toMap ++
    storage.collectFolderMetadata.map(f=>f.path -> f).toMap

  val staticPages: Map[String, CompactByteString] = paths.values.collect {
    case meta: PageMetadata => meta.path -> CompactByteString(Renderer.render(parser.treeNodes(meta)))
  }(collection.breakOut)

//  val staticPages: Map[String, CompactByteString] = paths.values.flatMap( meta => meta match {
//    case meta: PageMetadata => Renderer.render(parser.treeNodes(meta)) match {
//      case Right(html) => Some (meta.path -> CompactByteString(html))
//      case Left(error) =>
//        logger.error(error.message)
//        None
//    }
//    case _: Metadata => None
//  }).toMap

  private def calculateContentType(s: String, default: ContentType = ContentTypes.NoContentType) = ContentType.parse(s).getOrElse(default)

  val requestHandler: HttpRequest => HttpResponse = {
    case request @ HttpRequest(GET, uri, headers, _, _) => {
      request.discardEntityBytes() // important to drain incoming HTTP Entity stream
      pathHandler(uri.path.toString(), headers)
    }
    case request =>
      request.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(StatusCodes.BadRequest, entity = "Only GET is supported!")
  }

  private def pathHandler(path: String, headers: Seq[HttpHeader]): HttpResponse = {
    paths.get(path) match {
      case None => HttpResponse(StatusCodes.NotFound)
      case Some(c: HttpCacheableMetadata) => headers.find( h => h.name == "If-None-Match") match {
        case Some(inm) if(EntityTag(c.cache.etag).value == inm.value()) => HttpResponse(NotModified)
        case _ => c match {
            case p: PageMetadata => pageHandler(p, headers)
            case r: ResourceMetadata => resourceHandler(r, headers)
          }
        }
      case Some(f: FolderMetadata) => folderHandler(f, headers)
      case Some(_) => HttpResponse(StatusCodes.InternalServerError, entity = "Unknown type resource found!")
    }
  }

  def pageHandler(p: PageMetadata, headers: Seq[HttpHeader]) = staticPages.get(p.path) match {
    case None => HttpResponse(StatusCodes.InternalServerError, entity = "Only static pages are supported")
    case Some(content) => HttpResponse(status = StatusCodes.OK, cacheHeaders(p), entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, content))
  }

  def resourceHandler(r: ResourceMetadata, headers: Seq[HttpHeader]) = storage.readResourceData(r) match {
    case f: File if f.exists && f.isRegularFile => HttpResponse(status = StatusCodes.OK, cacheHeaders(r), entity = HttpEntity.fromFile(calculateContentType(r.mimeType), storage.readResourceData(r).toJava))
    case _ => HttpResponse(StatusCodes.NotFound, entity = HttpEntity("Ops! Found metadata but didn't find data."))
  }

  def folderHandler(f: FolderMetadata, headers: Seq[HttpHeader]) = f.defaultContent match {
    case Some(path) => paths.get(path) match {
      case Some(_: FolderMetadata) => HttpResponse(StatusCodes.InternalServerError, entity = HttpEntity("Folder as default content is not allowed. Please, fix the site."))
      case _ => pathHandler(path, headers)
    }
    case None if f.listContents => HttpResponse(StatusCodes.InternalServerError, entity = "List folder content is not implemented.")
    case _ => HttpResponse(StatusCodes.Forbidden, entity = HttpEntity("I'm not allowed to show the folder content."))
  }

  def cacheHeaders(meta: HttpCacheableMetadata) = immutable.Seq[HttpHeader]( ETag(meta.cache.etag) )

}
