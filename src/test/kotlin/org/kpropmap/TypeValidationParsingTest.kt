package org.kpropmap

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test
import org.kpropmap.test.SimpleValues

class TypeValidationParsingTest {
  @Test
  fun `Simple values of the wrong target type throw TypeMismatchException`() {
    val input = propMapOf(
      SimpleValues::intRegular to "6"
    )

    assertThat({ input[SimpleValues::intRegular] }, throws(has(TypeMismatchException::fieldPath, equalTo(listOf(SimpleValues::intRegular.name)))))
    assertThat({ input.required(SimpleValues::intRegular) }, throws(has(TypeMismatchException::fieldPath, equalTo(listOf(SimpleValues::intRegular.name)))))
  }
}
