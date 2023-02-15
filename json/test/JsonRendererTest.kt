package klite.json

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test

class JsonRendererTest {
  val mapper = JsonMapper()

  @Test fun literals() {
    expect(mapper.render(null)).toEqual("null")
    expect(mapper.render(true)).toEqual("true")
    expect(mapper.render(false)).toEqual("false")
    expect(mapper.render(123.45)).toEqual("123.45")
  }

  @Test fun string() {
    expect(mapper.render("Hello")).toEqual("\"Hello\"")
  }

  @Test fun array() {
    expect(mapper.render(emptyList<Any>())).toEqual("[]")
    expect(mapper.render(listOf("a", 1, 3))).toEqual("[\"a\",1,3]")
    expect(mapper.render(arrayOf(1, null))).toEqual("[1,null]")
  }
}
