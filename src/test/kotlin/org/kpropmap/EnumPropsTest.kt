package org.kpropmap

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.present
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test
import org.kpropmap.test.EnumClass
import org.kpropmap.test.EnumValues

class EnumPropsTest {
  @Test
  fun `Enum values are parsed`() {
    val input = propMapOf(
      EnumValues::enum to EnumClass.TEST1.name
    )

    assertThat(EnumValues::enum in input, equalTo(true))
    assertThat(input[EnumValues::enum], present(equalTo(EnumClass.TEST1)))
  }

  @Test
  fun `Enum values are not case sensitive`() {
    val input = propMapOf(
      EnumValues::enum to "Test1"
    )

    assertThat(EnumValues::enum in input, equalTo(true))
    assertThat(input[EnumValues::enum], present(equalTo(EnumClass.TEST1)))
  }

  @Test
  fun `Enum values that do not exist throw PropertyParsingException`() {
    val input = propMapOf(
      EnumValues::enum to "NOTEXIST"
    )

    assertThat(EnumValues::enum in input, equalTo(true))
    assertThat({ input[EnumValues::enum] },
      throws(has(PropertyParsingException::fieldPath, equalTo(listOf(EnumValues::enum.name)))))
  }
}
