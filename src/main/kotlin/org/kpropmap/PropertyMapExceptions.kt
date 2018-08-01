package org.kpropmap

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class TypeMismatchException(
  fieldPath: List<String>,
  val expectedType: KClass<*>,
  val receivedType: Class<*>
) : PropertyMapFieldPathException(fieldPath, "Type mismatch at field path $fieldPath, expected $expectedType, received $receivedType")

class PropertyParsingException(
  fieldPath: List<String>,
  message: String
) : PropertyMapFieldPathException(fieldPath, message)

/** When the input data is invalid. A reason will be provided. */
class InvalidInputDataException private constructor(
  val error: String,
  template: String,
  vararg templateArgs: Any?)
  : PropertyMapException(template.format(*templateArgs.map { when(it) { is KProperty<*> -> it.name else -> it } }.toTypedArray())) {

  constructor(reason: InvalidInputData, vararg templateArgs: Any?)
    : this(reason.error, reason.template, *templateArgs)
}

open class PropertyMapFieldPathException(
  val fieldPath: List<String>,
  message: String
) : PropertyMapException(message)

open class PropertyMapException(
  message: String
) : RuntimeException(message)
