package org.kpropmap

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.present
import org.junit.jupiter.api.Test
import org.kpropmap.test.NotExist
import org.kpropmap.test.SimpleValues

class PropertyMapCreationFromDataClassTest {
  @Test
  fun `Create and use a property map from a data class`() {
    val simpleValues = SimpleValues(
      intRegular = 1,
      intRegularNullable = 2,
      intJava = 3,
      stringRegular = "string1",
      stringRegularNullable = "string2",
      booleanTrue = true,
      booleanFalse = false,
      booleanNullable = true
    )

    val input = propMapOf(simpleValues)

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
}
