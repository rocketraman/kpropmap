package org.kpropmap

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.present
import org.junit.jupiter.api.Test
import org.kpropmap.test.DoubleValues
import org.kpropmap.test.ParameterizedValuesDouble

class DoublesTest {
  @Test
  fun `Doubles are parsed from standard number representations`() {
    val input = propMapOf(
      DoubleValues::val1 to 10.0,
      DoubleValues::val2 to 20
    )

    assertThat(input[DoubleValues::val1], present(equalTo(10.0)))
    // widening conversion int to double
    assertThat(input[DoubleValues::val2], present(equalTo(20.0)))
  }

  @Test
  fun `Doubles are parsed into a parameterized List`() {
    val input = propMapOf(
      ParameterizedValuesDouble::listDouble to listOf(10, 20.0, java.lang.Double("30.0"))
    )

    assertThat(input[ParameterizedValuesDouble::listDouble], present(equalTo(listOf(10.0, 20.0, 30.0))))
  }
}
