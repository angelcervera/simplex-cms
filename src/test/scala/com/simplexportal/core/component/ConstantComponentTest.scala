package com.simplexportal.core.component

import com.simplexportal.core.renderer.RenderContext
import org.scalatest.WordSpec

class ConstantComponentTest extends WordSpec {

  "ConstantComponent" should {
    "return the body content" when {
      "doesn't contain inner components" in {
        val result = ConstantComponent.render(ComponentMetadata(
          "id",
          "cnt",
          "<simplex:cnt id=\"component1\">Constant ${value}</simplex:cnt>",
          1
        ), RenderContext())
        assert(result === Right("Constant ${value}"))
      }
      "has inner components" in {
        val result = ConstantComponent.render(ComponentMetadata(
          "id",
          "cnt",
          "<simplex:cnt id=\"component1\">This contains an <simplex:render id=\"component2\">inner</simplex:render> value</simplex:cnt>",
          1
        ), RenderContext())
        assert(result === Right("This contains an <simplex:render id=\"component2\">inner</simplex:render> value"))
      }
    }
  }

}
