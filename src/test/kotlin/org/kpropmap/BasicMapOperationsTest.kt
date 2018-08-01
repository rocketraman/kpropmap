package org.kpropmap

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.present
import org.junit.jupiter.api.Test
import org.kpropmap.test.SimpleValues

class BasicMapOperationsTest {
  @Test
  fun `Gets typed values`() {
    val input = propMapOf(
      SimpleValues::stringRegular to null,
      SimpleValues::stringRegularNullable to null,
      SimpleValues::intRegular to 5,
      SimpleValues::intRegularNullable to 6
    )

    assertThat(input[SimpleValues::stringRegular], absent())
    assertThat(input[SimpleValues::stringRegularNullable], absent())
    assertThat(input[SimpleValues::intRegular], present(equalTo(5)))
    assertThat(input[SimpleValues::intRegularNullable], present(equalTo(6)))
  }

  @Test
  fun `Removes typed values`() {
    val input = propMapOf(
      SimpleValues::stringRegular to null,
      SimpleValues::stringRegularNullable to null,
      SimpleValues::intRegular to 5,
      SimpleValues::intRegularNullable to 6
    )

    assertThat(input.remove(SimpleValues::stringRegular), absent())
    assertThat(input.remove(SimpleValues::stringRegularNullable), absent())
    assertThat(input.remove(SimpleValues::intRegular), present(equalTo(5)))
    assertThat(input.remove(SimpleValues::intRegularNullable), present(equalTo(6)))
  }

  @Test
  fun `Contains typed values`() {
    val input = propMapOf(
      SimpleValues::stringRegular to null,
      SimpleValues::stringRegularNullable to null,
      SimpleValues::intRegular to 5,
      SimpleValues::intRegularNullable to 6
    )

    assertThat(SimpleValues::stringRegular in input, equalTo(true))
    assertThat(SimpleValues::stringRegularNullable in input, equalTo(true))
    assertThat(SimpleValues::intRegular in input, equalTo(true))
    assertThat(SimpleValues::intRegularNullable in input, equalTo(true))
    assertThat(SimpleValues::booleanFalse in input, equalTo(false))
    assertThat(SimpleValues::booleanNullable in input, equalTo(false))
  }
}
