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
      paths.get(uri.path.toString()) match {
        case None => HttpResponse(StatusCodes.NotFound)
        case Some(p: Page) => pageHandler(p)
        case Some(r: Resource) => resourceHandler(r)
        case Some(_) => HttpResponse(StatusCodes.InternalServerError, entity = "Unknown type resource found!")
      }
    }
    case request =>
      request.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(StatusCodes.BadRequest, entity = "Only GET is supported at the moment!")
  }


  def pageHandler(page: Page) = staticPages.get(page.metadata.path) match {
    case None => HttpResponse(StatusCodes.InternalServerError, entity = "Only static pages are supported")
    case Some(content) => HttpResponse(status = StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, content))
  }

  def resourceHandler(r: Resource) = storage.readResourceData(r.metadata) match {
    case f: File if f.exists && f.isRegularFile => HttpResponse(status = StatusCodes.OK, entity = HttpEntity.fromFile(calculateContentType(r.metadata.mimeType), r.metadata.data.toJava))
    case _ => HttpResponse(StatusCodes.NotFound, entity = HttpEntity("Ops! Found metadata but didn't find data."))
  }

}
