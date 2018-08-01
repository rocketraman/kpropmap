package org.kpropmap

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.kpropmap.test.*
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.reflect.full.memberProperties

class IntrospectionTest {
  @Test
  fun `Keys not in a given data class can be identified`() {
    val input = propMapOf(
      NotExist::foo to 1,
      IntValues::val1 to 1,
      IntValues::val2 to 2
    )

    input.keysNotInProps(IntValues::class.memberProperties).let {
      assertThat(it, present(hasSize(equalTo(1))))
      assertThat(it, present(allElements(equalTo<Any>(NotExist::foo.name))))
    }
  }
}
