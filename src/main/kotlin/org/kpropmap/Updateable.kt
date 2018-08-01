package org.kpropmap

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * An annotation that marks an individual property, or all the properties in a class,
 * as updateable.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class Updateable(
  /**
   * A mapped property is an Updateable property that itself contains one or more Updateable
   * properties.
   */
  val mapped: Boolean = false
)

fun updateablePropsOf(vararg kClass: KClass<*>) = kClass.asList()
  .flatMap { c -> c.memberProperties.map { it to c } }
  .filter { it.second.annotations.any { it is Updateable } || it.first.annotations.any { it is Updateable } }
  .map { it.first }

fun updateableTypedMapPropsOf(vararg kClass: KClass<*>) = kClass.asList()
  .flatMap { it.memberProperties }
  .filter { it.annotations.any { it is Updateable && it.mapped } }
