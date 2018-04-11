package com.simplexportal.core.webserver

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.util.CompactByteString
import better.files.File
import com.simplexportal.core._

trait RequestHandler {

  implicit val materializer: Materializer

  val storage = new Storage(Configuration.storage)
  import storage._

  val paths = storage.paths
  val staticPages = paths.values.collect {
    case page: Page => page.metadata.path -> CompactByteString(Renderer.render(page))
  }.toMap


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
      case Some(p: Page) => pageHandler(p)
      case Some(r: Resource) => resourceHandler(r)
      case Some(f: Folder) => folderHandler(f)
      case Some(_) => HttpResponse(StatusCodes.InternalServerError, entity = "Unknown type resource found!")
    }
  }

  def pageHandler(page: Page) = staticPages.get(page.metadata.path) match {
    case None => HttpResponse(StatusCodes.InternalServerError, entity = "Only static pages are supported")
    case Some(content) => HttpResponse(status = StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, content))
  }

  def resourceHandler(r: Resource) = storage.readResourceData(r.metadata) match {
    case f: File if f.exists && f.isRegularFile => HttpResponse(status = StatusCodes.OK, entity = HttpEntity.fromFile(calculateContentType(r.metadata.mimeType), r.metadata.data.toJava))
    case _ => HttpResponse(StatusCodes.NotFound, entity = HttpEntity("Ops! Found metadata but didn't find data."))
  }

  def folderHandler(f: Folder) = f.metadata.defaultContent match {
    case Some(path) => paths.get(path) match {
      case f: Folder => HttpResponse(StatusCodes.InternalServerError, entity = HttpEntity("Folder as default content is not allowed. Please, fix the site."))
      case _ => pathHandler(path)
    }
    case None if f.metadata.listContents => HttpResponse(StatusCodes.InternalServerError, entity = "List folder content is not implemented.")
    case _ => HttpResponse(StatusCodes.Forbidden, entity = HttpEntity("I'm not allowed to show the folder content."))
  }

}
