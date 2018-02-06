package com.simplexportal.core.datamodel

/**
  * General error in SimplexPortal.
  */
trait SimplexPortalError

case class GenericError(throwable: Throwable) extends SimplexPortalError
