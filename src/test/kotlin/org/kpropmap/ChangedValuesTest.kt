package org.kpropmap

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.present
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.kpropmap.test.BooleanValues
import org.kpropmap.test.IntValues
import org.kpropmap.test.NotExist
import org.kpropmap.test.SimpleValues

class ChangedValuesTest {
  data class SimpleNullable(val s: String?)

  @Test
  fun `Keys with changed values relative to an existing data class can be identified`() {
    val input = propMapOf(
      NotExist::foo to 1,
      IntValues::val1 to 1,
      IntValues::val2 to 2
    )

    val from1 = IntValues(1, 2, 3)
    val from2 = IntValues(10, 11, 12)

    assertThat(input.hasChanged(IntValues::val1, from1), equalTo(false))
    assertThat(input.hasChanged(IntValues::val2, from1), equalTo(false))
    assertThat(input.hasChanged(IntValues::val3, from1), equalTo(false))

    assertThat(input.hasChanged(IntValues::val1, from2), equalTo(true))
    assertThat(input.hasChanged(IntValues::val2, from2), equalTo(true))
    assertThat(input.hasChanged(IntValues::val3, from2), equalTo(false))
  }

  @Test
  fun `Keys with changed null values relative to an existing data class can be identified`() {
    val from1 = BooleanValues(true)
    val from2 = BooleanValues(false)
    val from3 = BooleanValues(null)

    propMapOf(
      BooleanValues::val1 to null
    ).let {
      assertThat(it.hasChanged(BooleanValues::val1, from1), equalTo(true))
      assertThat(it.hasChanged(BooleanValues::val1, from2), equalTo(true))
      assertThat(it.hasChanged(BooleanValues::val1, from3), equalTo(false))
    }

    propMapOf(
      BooleanValues::val1 to true
    ).let {
      assertThat(it.hasChanged(BooleanValues::val1, from1), equalTo(false))
      assertThat(it.hasChanged(BooleanValues::val1, from2), equalTo(true))
      assertThat(it.hasChanged(BooleanValues::val1, from3), equalTo(true))
    }
  }

  @Test
  fun `Keys with changed values relative to values calculated by lambda can be identified`() {
    val input = propMapOf(
      NotExist::foo to 1,
      IntValues::val1 to 1,
      IntValues::val2 to 2,
      SimpleValues::booleanNullable to true
    )

    assertThat(input.hasChanged(IntValues::val1) { 1 }, equalTo(false))
    assertThat(input.hasChanged(IntValues::val2) { 2 }, equalTo(false))
    assertThat(input.hasChanged(IntValues::val3) { 3 }, equalTo(false))  // not defined, so we assume no change

    assertThat(input.hasChanged(IntValues::val1) { 10 }, equalTo(true))
    assertThat(input.hasChanged(IntValues::val2) { 11 }, equalTo(true))
    assertThat(input.hasChanged(IntValues::val3) { 12 }, equalTo(false))

    assertThat(input.hasChanged(SimpleValues::booleanNullable) { true }, equalTo(false))
    assertThat(input.hasChanged(SimpleValues::booleanNullable) { false }, equalTo(true))
    assertThat(input.hasChanged(SimpleValues::booleanNullable) { null }, equalTo(true))
  }

  @Test
  fun `Runs code block conditional on a changed value relative to a data class`() {
    val input = propMapOf(
      IntValues::val1 to 1
    )

    input.withChanged(IntValues::val1, IntValues(1,2, 3)) {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(IntValues::val1, IntValues(4,5, 6)) {
      lambdaRan = true
      assertThat(it, present(equalTo(1)))
    }
    assertThat(lambdaRan, equalTo(true))
  }

  @Test
  fun `Runs code block conditional on a changed value relative to a lambda`() {
    val input = propMapOf(
      IntValues::val1 to 1
    )

    input.withChanged(IntValues::val1, { 1 }) {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(IntValues::val1, { 4 }) {
      lambdaRan = true
      assertThat(it, present(equalTo(1)))
    }
    assertThat(lambdaRan, equalTo(true))
  }

  @Test
  fun `Returns value conditional on a changed value relative to a data class`() {
    val input = propMapOf(
      IntValues::val1 to 1
    )

    input.withChanged(IntValues::val1, IntValues(1,2, 3))?.let {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(IntValues::val1, IntValues(4,5, 6))?.let {
      lambdaRan = true
      assertThat(it, present(equalTo(1)))
    }
    assertThat(lambdaRan, equalTo(true))
  }

  @Suppress("MoveLambdaOutsideParentheses")
  @Test
  fun `Returns value conditional on a changed value relative to a lambda`() {
    val input = propMapOf(
      IntValues::val1 to 1
    )

    input.withChanged(IntValues::val1, { 1 })?.let {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(IntValues::val1, { 4 })?.let {
      lambdaRan = true
      assertThat(it, present(equalTo(1)))
    }
    assertThat(lambdaRan, equalTo(true))
  }

  @Test
  fun `Runs code block conditional on a value changed from null relative to a data class`() {
    val input = propMapOf(
      SimpleNullable::s to "foo"
    )

    input.withChanged(SimpleNullable::s, SimpleNullable("foo")) {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(SimpleNullable::s, SimpleNullable(null)) {
      lambdaRan = true
      assertThat(it, present(equalTo("foo")))
    }
    assertThat(lambdaRan, equalTo(true))
  }

  @Test
  fun `Runs value conditional on a value changed from null relative to a lambda`() {
    val input = propMapOf(
      SimpleNullable::s to "foo"
    )

    input.withChanged(SimpleNullable::s, { "foo" }) {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(SimpleNullable::s, { null }) {
      lambdaRan = true
      assertThat(it, present(equalTo("foo")))
    }
    assertThat(lambdaRan, equalTo(true))
  }

  @Test
  fun `Returns value conditional on a value changed from null relative to a data class`() {
    val input = propMapOf(
      SimpleNullable::s to "foo"
    )

    input.withChanged (SimpleNullable::s, SimpleNullable("foo"))?.let {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(SimpleNullable::s, SimpleNullable(null))?.let {
      lambdaRan = true
      assertThat(it, present(equalTo("foo")))
    }
    assertThat(lambdaRan, equalTo(true))
  }

  @Suppress("MoveLambdaOutsideParentheses")
  @Test
  fun `Returns value conditional on a value changed from null relative to a lambda`() {
    val input = propMapOf(
      SimpleNullable::s to "foo"
    )

    input.withChanged(SimpleNullable::s, { "foo" })?.let {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(SimpleNullable::s, { null })?.let {
      lambdaRan = true
      assertThat(it, present(equalTo("foo")))
    }
    assertThat(lambdaRan, equalTo(true))
  }

  @Test
  fun `Runs code block conditional on a value changed to null relative to a data class`() {
    val input = propMapOf(
      SimpleNullable::s to null
    )

    input.withChanged(SimpleNullable::s, SimpleNullable(null)) {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(SimpleNullable::s, SimpleNullable("foo")) {
      lambdaRan = true
      assertThat(it, absent())
    }
    assertThat(lambdaRan, equalTo(true))
  }

  @Test
  fun `Runs code block conditional on a value changed to null relative to a lambda`() {
    val input = propMapOf(
      SimpleNullable::s to null
    )

    input.withChanged(SimpleNullable::s, { null }) {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(SimpleNullable::s, { "foo" }) {
      lambdaRan = true
      assertThat(it, absent())
    }
    assertThat(lambdaRan, equalTo(true))
  }

  @Test
  fun `Returns value conditional on a value changed to null relative to a data class`() {
    val input = propMapOf(
      SimpleNullable::s to null
    )

    input.withChanged(SimpleNullable::s, SimpleNullable(null))?.let {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(SimpleNullable::s, SimpleNullable("foo")).let {
      lambdaRan = true
      assertThat(it, absent())
    }
    assertThat(lambdaRan, equalTo(true))
  }

  @Suppress("MoveLambdaOutsideParentheses")
  @Test
  fun `Returns value conditional on a value changed to null relative to a lambda`() {
    val input = propMapOf(
      SimpleNullable::s to null
    )

    input.withChanged(SimpleNullable::s, { null })?.let {
      fail("This should not execute")
    }

    var lambdaRan = false
    input.withChanged(SimpleNullable::s, { "foo" }).let {
      lambdaRan = true
      assertThat(it, absent())
    }
    assertThat(lambdaRan, equalTo(true))
  }
}
