package org.kpropmap

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.present
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test
import org.kpropmap.test.DateTimeValues
import java.time.Instant
import java.time.OffsetDateTime

class DateTimePropsTest {
  @Test
  fun `Instant and OffsetDateTime values are parsed`() {
    val input = propMapOf(
      DateTimeValues::instantRegular to "2016-04-14T17:00:00Z",
      DateTimeValues::offsetDtRegular to "2016-04-14T17:00:00Z"
    )

    assertThat(DateTimeValues::instantRegular in input, equalTo(true))
    assertThat(DateTimeValues::offsetDtRegular in input, equalTo(true))

    assertThat(input[DateTimeValues::instantRegular], present(equalTo(Instant.parse("2016-04-14T17:00:00Z"))))
    assertThat(input[DateTimeValues::offsetDtRegular], present(equalTo(OffsetDateTime.parse("2016-04-14T17:00:00Z"))))
  }

  @Test
  fun `Invalid Instant and OffsetDateTime values throw PropertyParsingException`() {
    val input = propMapOf(
      DateTimeValues::instantRegular to "2016-04-40T17:00:00Z",
      DateTimeValues::offsetDtRegular to "2016-04-40T17:00:00Z"
    )

    assertThat(DateTimeValues::instantRegular in input, equalTo(true))
    assertThat(DateTimeValues::offsetDtRegular in input, equalTo(true))

    assertThat({ input[DateTimeValues::instantRegular] },
      throws(has(PropertyParsingException::fieldPath, equalTo(listOf(DateTimeValues::instantRegular.name)))))
    assertThat({ input[DateTimeValues::offsetDtRegular] },
      throws(has(PropertyParsingException::fieldPath, equalTo(listOf(DateTimeValues::offsetDtRegular.name)))))
  }
}
