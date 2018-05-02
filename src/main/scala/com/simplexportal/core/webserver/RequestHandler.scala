package com.simplexportal.core.webserver

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.util.CompactByteString
import better.files.File
import com.simplexportal.core._
import com.simplexportal.core.dao._
import com.simplexportal.core.dao.FileSystemStorage
import com.simplexportal.core.parser.Parser
import com.simplexportal.core.renderer.Renderer
import com.typesafe.scalalogging.LazyLogging

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
    case request @ HttpRequest(GET, uri, _, _, _) => {
      request.discardEntityBytes() // important to drain incoming HTTP Entity stream
      pathHandler(uri.path.toString())
    }
    case request =>
      request.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(StatusCodes.BadRequest, entity = "Only GET is supported at the moment!")
  }

  private def pathHandler(path: String): HttpResponse = {
    paths.get(path) match {
      case None => HttpResponse(StatusCodes.NotFound)
      case Some(p: PageMetadata) => pageHandler(p)
      case Some(r: ResourceMetadata) => resourceHandler(r)
      case Some(f: FolderMetadata) => folderHandler(f)
      case Some(_) => HttpResponse(StatusCodes.InternalServerError, entity = "Unknown type resource found!")
    }
  }

  def pageHandler(page: PageMetadata) = staticPages.get(page.path) match {
    case None => HttpResponse(StatusCodes.InternalServerError, entity = "Only static pages are supported")
    case Some(content) => HttpResponse(status = StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, content))
  }

  def resourceHandler(r: ResourceMetadata) = storage.readResourceData(r) match {
    case f: File if f.exists && f.isRegularFile => HttpResponse(status = StatusCodes.OK, entity = HttpEntity.fromFile(calculateContentType(r.mimeType), storage.readResourceData(r).toJava))
    case _ => HttpResponse(StatusCodes.NotFound, entity = HttpEntity("Ops! Found metadata but didn't find data."))
  }

  def folderHandler(f: FolderMetadata) = f.defaultContent match {
    case Some(path) => paths.get(path) match {
      case Some(_: FolderMetadata) => HttpResponse(StatusCodes.InternalServerError, entity = HttpEntity("Folder as default content is not allowed. Please, fix the site."))
      case _ => pathHandler(path)
    }
    case None if f.listContents => HttpResponse(StatusCodes.InternalServerError, entity = "List folder content is not implemented.")
    case _ => HttpResponse(StatusCodes.Forbidden, entity = HttpEntity("I'm not allowed to show the folder content."))
  }

}
