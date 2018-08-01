package org.kpropmap

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.present
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test
import org.kpropmap.test.BooleanValues
import org.kpropmap.test.IntValues
import org.kpropmap.test.NotExist
import org.kpropmap.test.SimpleValues

class PropertyMapTypedCreationTest {
  @Test
  fun `Create and use a property map from Pairs with String keys`() {
    val input = propMapOf(
      "intRegular" to 1,
      "intRegularNullable" to 2,
      "intJava" to Integer(3),
      "stringRegular" to "string1",
      "stringRegularNullable" to "string2",
      "booleanTrue" to true,
      "booleanFalse" to false,
      "booleanNullable" to true
    )

    assertThat(SimpleValues::intRegular in input, equalTo(true))
    assertThat(SimpleValues::intRegularNullable in input, equalTo(true))
    assertThat(SimpleValues::stringRegular in input, equalTo(true))
    assertThat(SimpleValues::stringRegularNullable in input, equalTo(true))
    assertThat(SimpleValues::booleanTrue in input, equalTo(true))
    assertThat(NotExist::foo in input, equalTo(false))
    assertThat("randomValue" in input, equalTo(false))

    assertThat(input[SimpleValues::intRegular] is Int, equalTo(true))
    assertThat(input[SimpleValues::stringRegular] is String, equalTo(true))
    assertThat(input[SimpleValues::booleanTrue] is Boolean, equalTo(true))

    assertThat(input[SimpleValues::intRegular], present(equalTo(1)))
    assertThat(input[SimpleValues::intJava], present(equalTo(3)))
    assertThat(input[SimpleValues::stringRegular] is String, equalTo(true))
    assertThat(input[SimpleValues::booleanTrue], present(equalTo(true)))
    assertThat(input[SimpleValues::booleanFalse], present(equalTo(false)))

    input[SimpleValues::stringRegular] = "Foo"
    assertThat(input[SimpleValues::stringRegular], present(equalTo("Foo")))
  }

  @Test
  fun `Create and use a property map over a regular Map with String keys`() {
    val input = propMapOf(mapOf(
      "intRegular" to 1,
      "intRegularNullable" to 2,
      "intJava" to Integer(3),
      "stringRegular" to "string1",
      "stringRegularNullable" to "string2",
      "booleanTrue" to true,
      "booleanFalse" to false,
      "booleanNullable" to true
    ))

    assertThat(SimpleValues::intRegular in input, equalTo(true))
    assertThat(SimpleValues::intRegularNullable in input, equalTo(true))
    assertThat(SimpleValues::stringRegular in input, equalTo(true))
    assertThat(SimpleValues::stringRegularNullable in input, equalTo(true))
    assertThat(SimpleValues::booleanTrue in input, equalTo(true))
    assertThat(NotExist::foo in input, equalTo(false))
    assertThat("randomValue" in input, equalTo(false))

    assertThat(input[SimpleValues::intRegular] is Int, equalTo(true))
    assertThat(input[SimpleValues::stringRegular] is String, equalTo(true))
    assertThat(input[SimpleValues::booleanTrue] is Boolean, equalTo(true))

    assertThat(input[SimpleValues::intRegular], present(equalTo(1)))
    assertThat(input[SimpleValues::intJava], present(equalTo(3)))
    assertThat(input[SimpleValues::stringRegular] is String, equalTo(true))
    assertThat(input[SimpleValues::booleanTrue], present(equalTo(true)))
    assertThat(input[SimpleValues::booleanFalse], present(equalTo(false)))

    input[SimpleValues::stringRegular] = "Foo"
    assertThat(input[SimpleValues::stringRegular], present(equalTo("Foo")))
  }

  @Test
  fun `Create and use a property map with KProperty keys`() {
    val input = propMapOf(
      SimpleValues::intRegular to 1,
      SimpleValues::intRegularNullable to 2,
      SimpleValues::intJava to Integer(3),
      SimpleValues::stringRegular to "string1",
      SimpleValues::stringRegularNullable to "string2",
      SimpleValues::booleanTrue to true,
      SimpleValues::booleanFalse to false,
      SimpleValues::booleanNullable to true
    )

    assertThat(SimpleValues::intRegular in input, equalTo(true))
    assertThat(SimpleValues::intRegularNullable in input, equalTo(true))
    assertThat(SimpleValues::stringRegular in input, equalTo(true))
    assertThat(SimpleValues::stringRegularNullable in input, equalTo(true))
    assertThat(SimpleValues::booleanTrue in input, equalTo(true))
    assertThat(NotExist::foo in input, equalTo(false))
    assertThat("randomValue" in input, equalTo(false))

    assertThat(input[SimpleValues::intRegular] is Int, equalTo(true))
    assertThat(input[SimpleValues::stringRegular] is String, equalTo(true))
    assertThat(input[SimpleValues::booleanTrue] is Boolean, equalTo(true))

    assertThat(input[SimpleValues::intRegular], present(equalTo(1)))
    assertThat(input[SimpleValues::intJava], present(equalTo(3)))
    assertThat(input[SimpleValues::stringRegular] is String, equalTo(true))
    assertThat(input[SimpleValues::booleanTrue], present(equalTo(true)))
    assertThat(input[SimpleValues::booleanFalse], present(equalTo(false)))

    input[SimpleValues::stringRegular] = "Foo"
    assertThat(input[SimpleValues::stringRegular], present(equalTo("Foo")))
  }

  @Test
  fun `Creating a PropertyMap where the KProperty values map to the same String is not supported`() {
    // because we create a PropertyMap keyed on String values, we can't use two different KProperty as keys
    // if they have the same name -- what would it take to relax this restriction? -- can we make the key a
    // KProperty instead of a String, and then provide the properties when converting from Strings?

    assertThat({ propMapOf(
      IntValues::val1 to 1,
      BooleanValues::val1 to true
    ) }, throws<AssertionError>() )
  }
}
