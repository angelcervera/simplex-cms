package com.simplexportal.core.dao

import java.nio.file.Paths

import better.files._
import com.simplexportal.core.dao.DataModel._
import org.scalatest.WordSpec

class DataModelTest extends WordSpec {

  "JacksonUtilities" should {
    "serialize/deserialize" when {
//      "try Empty" in {
//        val e = PageMetadata(path="/xx/xx", template = "/templateXX/XX")
//
//        Paths.get("target/out/jackson/Empty.json").marshall(e)
//        val x = Paths.get("target/out/jackson/Empty.json").unmarshall
//
//
////        val result = Metadata.mapper.readValue(Paths.get("target/out/jackson/Empty.json").toFile, classOf[Empty])
//
////        val result: Empty = e.load(Paths.get("target/out/jackson/Empty.json"))
//
//        // assert(result == e)
//      }

      "try PageMetadata" in {
        val page = PageMetadata(path="/xx/xx", template = "/templateXX/XX")

        val path = Paths.get("target/out/jackson/PageMetadata.json")
        path.toFile.toScala.parent.createDirectories



      }

    }
  }

}
