package org.kpropmap

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.reflect.full.memberProperties

class ListToPairTest {

  data class ParameterizedValuesPair(
    val pairStringInt: Pair<String, Int>
  )

  @Test
  fun `Lists of size 2 can be converted to a Pair`() {
    val input = propMapOf(
      ParameterizedValuesPair::pairStringInt to listOf("foo", 10)
    )

    assertThat(input[ParameterizedValuesPair::pairStringInt], present(equalTo("foo" to 10)))
  }

  @Test
  fun `Lists of size != 2 cannot be converted to a Pair`() {
    propMapOf(
      ParameterizedValuesPair::pairStringInt to listOf("foo", 10, 5)
    ).let {
      assertThat({ it[ParameterizedValuesPair::pairStringInt] }, throws(
        has(PropertyParsingException::fieldPath, equalTo(listOf(ParameterizedValuesPair::pairStringInt.name))))
      )
    }

    propMapOf(
      ParameterizedValuesPair::pairStringInt to listOf("foo")
    ).let {
      assertThat({ it[ParameterizedValuesPair::pairStringInt] }, throws(
        has(PropertyParsingException::fieldPath, equalTo(listOf(ParameterizedValuesPair::pairStringInt.name))))
      )
    }
  }

  @Test
  fun `Lists of size 2, but with the incorrect Pair types, throw TypeMismatchException`() {
    propMapOf(
      ParameterizedValuesPair::pairStringInt to listOf("foo", "bar")
    ).let {
      assertThat({ it[ParameterizedValuesPair::pairStringInt] }, throws(
        has(TypeMismatchException::fieldPath, equalTo(listOf(ParameterizedValuesPair::pairStringInt.name))))
      )
    }
  }
}
