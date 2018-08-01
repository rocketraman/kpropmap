package org.kpropmap

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test

class CheckFieldsExtensionsTest {
  data class CheckUpdateable(
    val s1: String,
    val s2: String,
    @Updateable val s3: String,
    @Updateable val s4: String
  )

  data class CheckUpdateableParent(
    @Updateable(mapped = true) val c1: CheckUpdateable,
    @Updateable(mapped = true) val c2: CheckUpdateable
  )

  @Test
  fun `Can check invalid fields given a PropertyMap and one or more classes`() {
    propMapOf(
      CheckUpdateable::s1 to "abc"
    ).let {
      assertThat({it.checkInvalid(CheckUpdateable::class)},
        throws(has(InvalidInputDataException::error, equalTo(InvalidInputData.FIELD_NOT_ALLOWED.error))))
    }

    propMapOf(
      CheckUpdateable::s3 to "abc"
    ).let {
      assertThat({it.checkInvalid(CheckUpdateable::class)}, ! throws<InvalidInputDataException>())
    }

    propMapOf(
      CheckUpdateableParent::c1 to propMapOf(
        CheckUpdateable::s1 to "abc"
      ),
      CheckUpdateableParent::c2 to propMapOf(
        CheckUpdateable::s3 to "abc"
      )
    ).let {
      assertThat({it.checkInvalid(CheckUpdateableParent::class)},
        throws(has(InvalidInputDataException::error, equalTo(InvalidInputData.FIELD_NOT_ALLOWED.error))))
    }

    propMapOf(
      CheckUpdateableParent::c1 to propMapOf(
        CheckUpdateable::s3 to "abc-1"
      ),
      CheckUpdateableParent::c2 to propMapOf(
        CheckUpdateable::s3 to "abc-2"
      )
    ).let {
      assertThat({it.checkInvalid(CheckUpdateableParent::class)}, ! throws<InvalidInputDataException>())
    }
  }

  @Test
  fun `Can check provided fields are not null given a PropertyMap and one or more classes`() {
    propMapOf(
      CheckUpdateable::s3 to "abc"
    ).let {
      assertThat({it.checkRequired(CheckUpdateable::class)}, ! throws<InvalidInputDataException>())
    }

    propMapOf(
      CheckUpdateable::s3 to null
    ).let {
      assertThat({it.checkRequired(CheckUpdateable::class)},
        throws(has(InvalidInputDataException::error, equalTo(InvalidInputData.FIELD_REQUIRED.error))))
    }

    propMapOf(
      CheckUpdateable::s3 to null
    ).let {
      assertThat({it.checkRequired(CheckUpdateable::class, exclude = listOf(CheckUpdateable::s3))},
        ! throws<InvalidInputDataException>())
    }
  }
}
