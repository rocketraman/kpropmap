package org.kpropmap

import java.lang.Exception
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

fun PropertyMap.checkInvalid(vararg classes: KClass<*>, path: List<String> = emptyList()) {
  keysNotIn(updateablePropsOf(*classes).map { it.name }).apply {
    if(isNotEmpty()) throw InvalidInputDataException(InvalidInputData.FIELD_NOT_ALLOWED, fieldString(path))
  }

  updateableTypedMapPropsOf(*classes).forEach {
    val prop = it.name
    val value = this[prop]
    if(value is Map<*, *> && value.filterKeys { it !is String }.isEmpty()) {
      @Suppress("UNCHECKED_CAST")
      PropertyMap(value as Map<String, Any?>).checkInvalid((it.returnType.javaType as Class<*>).kotlin, path = path + prop)
    } else if(value != null) {
      throw InvalidInputDataException(InvalidInputData.FIELD_CONTENT_INVALID, listOf(prop).fieldString(path), "expected object")
    }
  }
}

fun PropertyMap.checkRequired(vararg classes: KClass<*>, path: List<String> = emptyList(),
  exclude: List<KProperty1<*, *>>? = null) {

  val requiredButNull = updateablePropsOf(*classes).filterNot { exclude != null && exclude.contains(it) }.mapNotNull {
    val prop = it.name
    if(prop in this && this[prop] == null && !it.returnType.isMarkedNullable) prop else null
  }

  if(requiredButNull.isNotEmpty()) {
    throw InvalidInputDataException(InvalidInputData.FIELD_REQUIRED, requiredButNull.fieldString(path))
  }
}

inline fun <reified T: Any> PropertyMap.deserialize(): T {
  try {
    return convertMapType(this, T::class.java) as T
  } catch(e: Exception) { when(e) {
    is TypeMismatchException -> throw TypeMismatchException(e.fieldPath, e.expectedType, e.receivedType)
    is PropertyParsingException -> throw PropertyParsingException(e.fieldPath, e.message ?: "Unable to parse ${T::class.java.typeName}")
    else -> throw PropertyParsingException(emptyList(), e.message ?: "Unable to construct ${T::class.java.typeName}")
  }}
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> PropertyMap.applyProps(target: T, exclude: List<KProperty1<T, *>>? = null): T {
  val applyTo = if(exclude != null && exclude.isNotEmpty()) {
    PropertyMap(this).apply {
      exclude.forEach { remove(it) }
    }
  } else this

  return applyProps(applyTo, target, T::class.members.find { it.name == "copy" }!!, T::class.memberProperties) as T
}

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun applyProps(propMap: PropertyMap, target: Any, copyMethod: KCallable<*>, properties: Collection<KProperty1<*, *>>): Any? {
  val callMap = copyMethod.parameters.mapNotNull { param ->
    when {
      param.kind == KParameter.Kind.INSTANCE -> param to target
      param.kind == KParameter.Kind.VALUE && param.name in propMap -> {
        val property: KProperty<Any?> = properties.find { it.name == param.name } ?:
          throw InvalidInputDataException(InvalidInputData.FIELD_NOT_ALLOWED, param.name)
        val value = propMap[property.name]
        val propertyReturnType = property.returnType.javaType

        param to if(value is Map<*, *> && propertyReturnType is Class<*> && propertyReturnType.isKotlinClass()
          && propertyReturnType.kotlin.members.any { it.name == "copy" }
          && target.javaClass.isKotlinClass()) {

          val nestedTarget = target.javaClass.kotlin.memberProperties.find { it.name == property.name }?.invoke(target)
          if(nestedTarget != null) {
            applyProps(
              PropertyMap(value as Map<String, Any>),
              nestedTarget,
              propertyReturnType.kotlin.members.find { it.name == "copy" }!!,
              propertyReturnType.kotlin.memberProperties
            )
          } else {
            propMap[property]
          }
        } else {
          // use the KProperty rather than the name to access the value so type conversions are done in PropertyMap
          propMap[property]
        }
      }
      else -> null
    }
  }.toMap()
  return copyMethod.callBy(callMap)
}

/**
 * Get a required property. The property must be in the map, and must not be null. If the property must be
 * in the map, but is allowed to be null, then use [requiredNullable].
 */
inline fun <reified T : Any?> PropertyMap.required(prop: KProperty<T>): T =
  this[prop] ?: throw InvalidInputDataException(InvalidInputData.FIELD_REQUIRED, prop)

/**
 * Get a required nullable property. The property must be in the map, but map be null. If the property must
 * both be in the map and must not be null, then use [required].
 */
inline fun <reified T : Any?> PropertyMap.requiredNullable(prop: KProperty<T>): T? {
  if(!prop.returnType.isMarkedNullable) throw IllegalArgumentException("requiredNullable must only be used with nullable props")
  if(prop !in this) throw InvalidInputDataException(InvalidInputData.FIELD_REQUIRED, prop)
  return this[prop]
}

private fun Iterable<String>.fieldString(path: List<String> = emptyList()) = StringBuilder(32).apply {
  append(joinToString(", "))
  if(!path.isEmpty()) append(""" @ ${path.joinToString(".")}""")
}.toString()

private const val metadataFqName = "kotlin.Metadata"

private fun Class<*>.isKotlinClass() =
  this.declaredAnnotations.singleOrNull { it.annotationClass.java.name == metadataFqName } != null
