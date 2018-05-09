package com.simplexportal.core.dao

import com.simplexportal.core.dao.HttpCacheability.Cacheability

sealed trait Metadata {
  val path: String
}

sealed trait HttpCacheableMetadata extends Metadata{
  val cache: HttpCache
}

object HttpCacheability extends Enumeration {
  type Cacheability = Value
  val public, `private`, `no-cache`, `only-if-cached` = Value
}

case class HttpCacheExpiration(maxAge: Long = 60*60*2 /*2 hours*/)

// TODO: Full implementation: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control
case class HttpCacheControl(
  cacheability: Cacheability = HttpCacheability.public,
  expiration: HttpCacheExpiration = HttpCacheExpiration()
)

case class HttpCache(etag: String, cacheControl: HttpCacheControl = HttpCacheControl())

case class PageMetadata(
  path: String,
  cache: HttpCache = HttpCache("1"),
  template: String,
  encoding: String = "UTF-8",
  mimeType: String = "text/html"
) extends HttpCacheableMetadata

case class ComponentMetadata(
  path: String,
  `type`: String,
  name: String,
  orderExecution: Int,
  transformers: Seq[String],
  parameters: Map[String, String]
) extends Metadata

case class TemplateMetadata(path: String, encoding: String, mimeType: String) extends Metadata

case class ResourceMetadata(
  path: String,
  cache: HttpCache = HttpCache("1"),
  encoding: String,
  mimeType: String
) extends HttpCacheableMetadata

case class FolderMetadata(path: String, defaultContent: Option[String], listContents: Boolean) extends Metadata
