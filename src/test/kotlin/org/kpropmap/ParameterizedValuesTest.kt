package org.kpropmap

import com.natpryce.hamkrest.allElements
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.or
import com.natpryce.hamkrest.present
import org.junit.jupiter.api.Test
import org.kpropmap.test.ParameterizedValues

class ParameterizedValuesTest {
  @Test
  fun `Parameterized values can be parsed`() {
    val input = propMapOf(
      ParameterizedValues::listString to listOf("a", "b"),
      ParameterizedValues::listStringNullable to listOf("a", "b"),
      ParameterizedValues::setString to setOf("setA", "setB"),
      ParameterizedValues::mapStringAny to mapOf("a" to "x", "b" to 1)
    )

    assertThat(input[ParameterizedValues::listString], present(equalTo(listOf("a", "b"))))
    assertThat(input[ParameterizedValues::listStringNullable], present(equalTo(listOf("a", "b"))))
    assertThat(input[ParameterizedValues::setString], present(allElements(equalTo("setA") or equalTo("setB"))))

    val actualMap = input[ParameterizedValues::mapStringAny]!!
    assertThat(actualMap.keys, present(allElements(equalTo("a") or equalTo("b"))))
    assertThat(actualMap.values, present(allElements(equalTo<Any?>("x") or equalTo<Any?>(1))))
  }
}
