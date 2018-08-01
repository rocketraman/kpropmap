package org.kpropmap

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.kpropmap.test.*
import java.time.Instant
import java.time.OffsetDateTime

class DataClassConstructionTest {
  data class Simple(val s: String, val i: Int)

  @Test
  fun `Simple values can be reconstructed`() {
    val input = propMapOf(
      Simple::s to "abc",
      Simple::i to 5
    )

    assertThat(input.deserialize(), equalTo(Simple("abc", 5)))
  }

  @Test
  fun `Simple values without all the fields available cannot be reconstructed`() {
    val input = propMapOf(
      Simple::s to "abc"
    )

    assertThat({ input.deserialize<Simple>() }, throws<PropertyParsingException>())
  }

  @Test
  fun `Simple values can be nested and reconstructed`() {
    val input = propMapOf(
      NestedParentWithNestedSimple::val1 to propMapOf(
        NestedSimpleValues::val1 to "a",
        NestedSimpleValues::val2 to "b"
      )
    )

    assertThat(NestedParentWithNestedSimple::val1 in input, equalTo(true))
    assertThat(input[NestedParentWithNestedSimple::val1], present(equalTo(NestedSimpleValues("a", "b"))))

    assertThat(input.deserialize(),
      equalTo(NestedParentWithNestedSimple(NestedSimpleValues("a", "b"))))
  }

  @Test
  fun `Nested simple values that cannot be constructed due to missing properties throw PropertyParsingException`() {
    val input = propMapOf(
      NestedParentWithNestedSimple::val1 to propMapOf(
        NestedSimpleValues::val1 to "a"
        // missing NestedSimpleValues::val2
      )
    )

    assertThat(NestedParentWithNestedSimple::val1 in input, equalTo(true))
    assertThat({ input[NestedParentWithNestedSimple::val1] },
      throws(has(PropertyParsingException::fieldPath, equalTo(listOf(NestedParentWithNestedSimple::val1.name)))))
  }

  @Test
  fun `Complex values can be nested and reconstructed`() {
    val input = propMapOf(
      NestedParentWithNestedComplex::val1 to propMapOf(
        DateTimeValues::instantRegular to "2016-04-14T17:00:00Z",
        DateTimeValues::offsetDtRegular to "2016-04-14T17:00:00Z"
      )
    )

    assertThat(NestedParentWithNestedComplex::val1 in input, equalTo(true))
    assertThat(input[NestedParentWithNestedComplex::val1],
      present(equalTo(DateTimeValues(Instant.parse("2016-04-14T17:00:00Z"), OffsetDateTime.parse("2016-04-14T17:00:00Z")))))
  }

  @Test
  fun `Complex null values can be nested and reconstructed`() {
    val input = propMapOf(
      NestedParentWithNestedComplex::val1 to null
    )

    assertThat(NestedParentWithNestedComplex::val1 in input, equalTo(true))
    assertThat(input[NestedParentWithNestedComplex::val1], absent())
  }

  @Test
  fun `Complex values that are nested and invalid throw PropertyParsingException on reconstruction`() {
    val input = propMapOf(
      NestedParentWithNestedComplex::val1 to propMapOf(
        DateTimeValues::instantRegular to "abc",
        DateTimeValues::offsetDtRegular to "2016-04-14T17:00:00Z"
      )
    )

    assertThat(NestedParentWithNestedComplex::val1 in input, equalTo(true))
    assertThat({ input[NestedParentWithNestedComplex::val1] },
      throws(has(PropertyParsingException::fieldPath,
        equalTo(listOf(NestedParentWithNestedComplex::val1.name, DateTimeValues::instantRegular.name)))))
  }
}
