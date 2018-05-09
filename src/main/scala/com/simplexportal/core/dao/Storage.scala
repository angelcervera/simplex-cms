package com.simplexportal.core.dao

import better.files.File
import com.simplexportal.core.dao._

trait Storage {


  def readTemplateData(template: TemplateMetadata): String

  def readComponentData(component: ComponentMetadata): String

  // TODO: Avoid to use File as interface definition
  def readResourceData(resource: ResourceMetadata) : File

  def writePageMetadata(page: PageMetadata)

  def collectPageMetadata: Seq[PageMetadata]

  def writeResourceMetadata(resource: ResourceMetadata)

  def collectResourceMetadata: Seq[ResourceMetadata]

  def collectTemplateMetadata: Seq[TemplateMetadata]

  def collectFolderMetadata: Seq[FolderMetadata]

  def collectComponentMetadata(pageMetadata: PageMetadata): Seq[ComponentMetadata]

}
