package org.kpropmap

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.present
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test
import org.kpropmap.test.SimpleValues

class RequiredExtensionsTest {
  @Test
  fun `Required extension returns non-nullable properties and errors on properties null or not present`() {
    val input = propMapOf(
      SimpleValues::stringRegular to null,
      SimpleValues::stringRegularNullable to null,
      SimpleValues::intRegular to 5,
      SimpleValues::intRegularNullable to 6
    )

    assertThat({ input.required(SimpleValues::stringRegular) }, throws<InvalidInputDataException>())
    assertThat({ input.required(SimpleValues::stringRegularNullable) }, throws<InvalidInputDataException>())
    assertThat(input.required(SimpleValues::intRegular), equalTo(5))
    assertThat(input.required(SimpleValues::intRegularNullable), equalTo(6))

    assertThat({ input.required(SimpleValues::booleanFalse) }, throws<InvalidInputDataException>())
    assertThat({ input.required(SimpleValues::booleanNullable) }, throws<InvalidInputDataException>())
  }

  @Test
  fun `Required nullable extension returns nullables and errors on nullable and non-nullable properties not present`() {
    val input = propMapOf(
      SimpleValues::stringRegular to null,
      SimpleValues::stringRegularNullable to null,
      SimpleValues::booleanNullable to true
    )

    assertThat({ input.requiredNullable(SimpleValues::stringRegular) }, throws<IllegalArgumentException>())
    assertThat(input.requiredNullable(SimpleValues::stringRegularNullable), absent())
    assertThat({ input.requiredNullable(SimpleValues::intRegular) }, throws<IllegalArgumentException>())
    assertThat({ input.requiredNullable(SimpleValues::intRegularNullable) }, throws<InvalidInputDataException>())
    assertThat(input.requiredNullable(SimpleValues::booleanNullable), present(equalTo(true)))
  }
}
