package com.simplexportal.migration

import com.simplexportal.core.dao.{FileSystemStorage, HttpCache}

object UpgradeCacheInfo {

  def update(storagePath: String) = {
    val storage = new FileSystemStorage(storagePath)
    storage.collectPageMetadata.foreach(p=> storage.writePageMetadata(p.copy(cache = HttpCache("1"))))
    storage.collectResourceMetadata.foreach(r=> storage.writeResourceMetadata(r.copy(cache = HttpCache("1"))))
  }

}

object UpgradeCacheInfoMain extends App {
  UpgradeCacheInfo.update(args(0))
}
