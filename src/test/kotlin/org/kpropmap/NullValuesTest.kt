package org.kpropmap

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.kpropmap.test.*

class NullValuesTest {
  @Suppress("USELESS_IS_CHECK")
  @Test
  fun `Null values are handled`() {
    val input = propMapOf(
      SimpleValues::intRegularNullable to null,
      SimpleValues::stringRegularNullable to null,
      SimpleValues::booleanNullable to null,
      ParameterizedValues::listStringNullable to null
    )

    assertThat(SimpleValues::intRegularNullable in input, equalTo(true))
    assertThat(SimpleValues::stringRegularNullable in input, equalTo(true))
    assertThat(SimpleValues::booleanNullable in input, equalTo(true))

    assertThat(input[SimpleValues::intRegularNullable] is Int?, equalTo(true))
    assertThat(input[SimpleValues::stringRegularNullable] is String?, equalTo(true))
    assertThat(input[SimpleValues::booleanNullable] is Boolean?, equalTo(true))

    assertThat(input[SimpleValues::intRegularNullable], absent())
    assertThat(input[SimpleValues::stringRegularNullable], absent())
    assertThat(input[SimpleValues::booleanNullable], absent())

    assertThat(input[ParameterizedValues::listStringNullable] is List<String>?, equalTo(true))
    assertThat(input[ParameterizedValues::listStringNullable], absent())

    input[SimpleValues::stringRegular] = "Foo"
    assertThat(input[SimpleValues::stringRegular], present(equalTo("Foo")))
  }

  @Test
  fun `Reading a list with null values against a List property with non-null values throws PropertyParsingException`() {
    val input = propMapOf(
      ParameterizedValues::listStringNullable to listOf(null)
    )

    assertThat({ input[ParameterizedValues::listStringNullable] },
      throws(has(PropertyParsingException::fieldPath, equalTo(listOf(ParameterizedValues::listStringNullable.name)))))
  }

  @Test
  fun `Reading a list with non-null values against a List property with non-null values`() {
    val input = propMapOf(
      ParameterizedValues::listString to listOf("a", "b", "c")
    )

    assertThat(input[ParameterizedValues::listString], equalTo(listOf("a", "b", "c")))
  }

  @Test
  fun `Reading a list with null and non-null values against a List property with nullable values`() {
    val input = propMapOf(
      ParameterizedValuesListNullableValues::listNullableString to listOf(null, "abc"),
      ParameterizedValuesListNullableValues::listNullableStringNullable1 to null,
      ParameterizedValuesListNullableValues::listNullableStringNullable2 to listOf(null)
    )

    assertThat(input[ParameterizedValuesListNullableValues::listNullableString], present(equalTo(listOf(null, "abc"))))
    assertThat(input[ParameterizedValuesListNullableValues::listNullableStringNullable1], absent())
    assertThat(input[ParameterizedValuesListNullableValues::listNullableStringNullable2], present(equalTo(listOf<String?>(null))))
  }

  @Test
  fun `Null values inside typed List with non-null contents throws PropertyParsingException`() {
    val input = propMapOf(
      ParameterizedValuesTypeVar<Bar>::listFoo1 to listOf(null, Bar("bar1")),
      ParameterizedValuesTypeVar<Bar>::listFoo2 to null
    )

    assertThat({ input[ParameterizedValuesTypeVar<Bar>::listFoo1] },
      throws(has(PropertyParsingException::fieldPath, equalTo(listOf(ParameterizedValuesTypeVar<Bar>::listFoo1.name)))))
    assertThat(input[ParameterizedValuesTypeVar<Bar>::listFoo2], absent())
  }

  @Test
  fun `Null values inside typed List with nullable contents`() {
    val input = propMapOf(
      ParameterizedValuesTypeVar<Bar>::listNullableFoo1 to listOf(null, Bar("bar1")),
      ParameterizedValuesTypeVar<Bar>::listNullableFoo2 to null
    )

    assertThat(input[ParameterizedValuesTypeVar<Bar>::listNullableFoo1], present(equalTo(listOf<Foo?>(null, Bar("bar1")))))
    assertThat(input[ParameterizedValuesTypeVar<Bar>::listNullableFoo2], absent())
  }
}
