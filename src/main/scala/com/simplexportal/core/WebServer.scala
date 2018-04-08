package com.simplexportal.core

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.io.StdIn

object WebServer extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  // needed for the future map/flatmap in the end
  implicit val executionContext = system.dispatcher

  val storage = new Storage(Configuration.storage)
  import storage._

  val paths = storage.paths
  val staticPages = paths.values.collect {
    case page: Page => page.metadata.path -> Renderer.render(page)
  }.toMap

  private def calculateContentType(s: String, default: ContentType = ContentTypes.NoContentType) = ContentType.parse(s).getOrElse(default)

  val requestHandler: HttpRequest => HttpResponse = {
    case request @ HttpRequest(GET, uri, _, _, _) => paths.get(uri.path.toString()) match {
      case None =>
        request.discardEntityBytes() // important to drain incoming HTTP Entity stream
        HttpResponse(StatusCodes.NotFound)
      case Some(p: Page) => staticPages.get(uri.path.toString()) match {
        case None =>
          request.discardEntityBytes() // important to drain incoming HTTP Entity stream
          HttpResponse(StatusCodes.InternalServerError, entity = "Only static pages are supported")
        case Some(content) =>
          request.discardEntityBytes() // important to drain incoming HTTP Entity stream
          HttpResponse(status = StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, content))
      }
      case Some(r: Resource) =>
        request.discardEntityBytes() // important to drain incoming HTTP Entity stream
        HttpResponse(status = StatusCodes.OK, entity = HttpEntity.fromFile(calculateContentType(r.metadata.mimeType), r.metadata.data.toJava))
      case Some(_) =>
        request.discardEntityBytes() // important to drain incoming HTTP Entity stream
        HttpResponse(StatusCodes.InternalServerError, entity = "Unknown type resource found!")
    }
    case request =>
      request.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(StatusCodes.BadRequest, entity = "Only GET is supported at the moment!")
  }

  val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}