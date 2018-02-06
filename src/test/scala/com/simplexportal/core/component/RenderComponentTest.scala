package com.simplexportal.core.component

import com.simplexportal.core.renderer.RenderContext
import org.scalatest.WordSpec

class RenderComponentTest extends WordSpec {

  "RenderComponent" should {
    "render right content" when {
      "doesn't contain inner components" in {
        val result = RenderComponent.render(ComponentMetadata(
          "id",
          "cnt",
          "<simplex:cnt id=\"component1\">Constant value</simplex:cnt>",
          1
        ), RenderContext())
        assert(result === Right("Constant value"))
      }

      "has inner components" in {
        fail(new NotImplementedError())
      }
    }
  }
}
