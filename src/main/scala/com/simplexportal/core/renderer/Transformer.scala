package com.simplexportal.core.renderer

import com.simplexportal.core.dao.Metadata
import com.simplexportal.core.datamodel.SimplexPortalError

trait Transformer {
  def name: String
  def transform(metadata: Metadata, txt: String): Either[SimplexPortalError, String]
}
