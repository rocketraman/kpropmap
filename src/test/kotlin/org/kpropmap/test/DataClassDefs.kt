package org.kpropmap.test

import java.time.Instant
import java.time.OffsetDateTime

data class IntValues(
  val val1: Int,
  val val2: Int,
  val val3: Int
)

data class DoubleValues(
  val val1: Double,
  val val2: Double,
  val val3: Double
)

data class BooleanValues(
  val val1: Boolean?
)

data class SimpleValues(
  val intRegular: Int,
  val intRegularNullable: Int?,
  val intJava: Int,
  val stringRegular: String,
  val stringRegularNullable: String?,
  val booleanTrue: Boolean,
  val booleanFalse: Boolean,
  val booleanNullable: Boolean?
)

data class NestedParentWithNestedSimple(
  val val1: NestedSimpleValues
)

data class NestedSimpleValues(
  val val1: String,
  val val2: String
)

data class NestedParentWithNestedComplex(
  val val1: DateTimeValues?
)

data class NestedParentWithNestedParent(
  val val1: String,
  val val2: NestedParentWithNestedSimple
)

data class DateTimeValues(
  val instantRegular: Instant,
  val offsetDtRegular: OffsetDateTime
)

enum class EnumClass {
  TEST1,
  TEST2
}

data class EnumValues(
  val enum: EnumClass
)

data class ParameterizedValues(
  val listString: List<String>,
  val listStringNullable: List<String>?,
  val setString: Set<String>,
  val mapStringAny: Map<String, Any?>
)

data class ParameterizedValuesListNullableValues(
  val listNullableString: List<String?>,
  val listNullableStringNullable1: List<String?>?,
  val listNullableStringNullable2: List<String?>?
)

open class Foo(val foo: String)

@Suppress("EqualsOrHashCode")
class Bar(private val bar: String): Foo("foo-from-bar") {
  override fun equals(other: Any?): Boolean = other is Bar
    && foo == other.foo
    && bar == other.bar
}

data class ParameterizedValuesTypeVar<out T: Foo>(
  val listFoo1: List<T>?,
  val listFoo2: List<T>?,
  val listNullableFoo1: List<T?>?,
  val listNullableFoo2: List<T?>?
)

data class ParameterizedValuesDouble(
  val listDouble: List<Double>
)

data class NotExist(val foo: String)

