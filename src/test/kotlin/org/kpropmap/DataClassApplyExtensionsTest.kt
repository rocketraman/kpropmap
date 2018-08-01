package org.kpropmap

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.kpropmap.test.NestedParentWithNestedParent
import org.kpropmap.test.NestedParentWithNestedSimple
import org.kpropmap.test.NestedSimpleValues

class DataClassApplyExtensionsTest {
  @Test
  fun `Nested simple values with missing properties can be applied to an existing data structure`() {
    val original = NestedParentWithNestedSimple(
      val1 = NestedSimpleValues(val1 = "o_1", val2 = "o_2")
    )

    propMapOf(
      NestedParentWithNestedSimple::val1 to propMapOf(
        NestedSimpleValues::val1 to "n_1"
      )
    ).applyProps(original).let {
      assertThat(it.val1.val1, equalTo("n_1"))
      assertThat(it.val1.val2, equalTo("o_2"))
    }
  }

  @Test
  fun `Doubly nested simple values with missing properties can be applied to an existing data structure`() {
    val original = NestedParentWithNestedParent(
      val1 = "p_o_1",
      val2 = NestedParentWithNestedSimple(
        val1 = NestedSimpleValues(val1 = "o_1", val2 = "o_2")
      )
    )

    propMapOf(
      NestedParentWithNestedParent::val2 to propMapOf(
        NestedParentWithNestedSimple::val1 to propMapOf(
          NestedSimpleValues::val1 to "n_1"
        )
      )
    ).applyProps(original).let {
      assertThat(it.val1, equalTo("p_o_1"))
      assertThat(it.val2.val1.val1, equalTo("n_1"))
      assertThat(it.val2.val1.val2, equalTo("o_2"))
    }

    propMapOf(
      NestedParentWithNestedParent::val1 to "p_n_1",
      NestedParentWithNestedParent::val2 to propMapOf(
        NestedParentWithNestedSimple::val1 to propMapOf(
          NestedSimpleValues::val2 to "n_2"
        )
      )
    ).applyProps(original).let {
      assertThat(it.val1, equalTo("p_n_1"))
      assertThat(it.val2.val1.val1, equalTo("o_1"))
      assertThat(it.val2.val1.val2, equalTo("n_2"))
    }
  }
}
