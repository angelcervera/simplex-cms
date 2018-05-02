package com.simplexportal.core.datamodel

/**
  * General error in SimplexPortal.
  */
trait SimplexPortalError {
  val message: String
}

object GenericSimplexPortalError {
  def apply(throwable: Throwable): GenericSimplexPortalError = new GenericSimplexPortalError(throwable.getMessage, throwable)
}

case class GenericSimplexPortalError(message: String, throwable: Throwable) extends SimplexPortalError
