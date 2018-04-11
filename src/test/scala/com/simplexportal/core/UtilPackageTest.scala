package com.simplexportal.core

import org.scalatest.WordSpec
import com.simplexportal.core.util._

class UtilPackageTest extends WordSpec {

  "ToOption_String" should {
    "return None if it is empty or null" in {
      assert(toOption("") == None)
      assert(toOption(null) == None)
    }
    "return Some in the rest of cases" in {
      assert(toOption("XX") == Some("XX"))
    }
    "return the first non empty" in {
      assert(toOption(null, "", "something", "others") == Some("something"))
    }
    "return None if no empties" in {
      assert(toOption(null, "") == None)
    }
  }

}
