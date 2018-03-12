package com.simplexportal.core.datamodel

/**
  * General error in SimplexPortal.
  */
trait Error

case class GenericError(throwable: Throwable) extends Error
