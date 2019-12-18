package org.kpropmap

import java.lang.Exception
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.time.DateTimeException
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

/**
 * The primary data structure, implementing `Map[String, Any?]`, but adding utility functions to insert
 * and retrieve data in a type-safe manner.
 */
class PropertyMap : HashMap<String, Any?> {
  constructor() : super()
  constructor(m: Map<String, Any?>) : super(m)

  operator fun contains(prop: KProperty<*>) = this.containsKey(prop.name)

  operator fun <V : Any?> get(prop: KProperty<V>): V? {
    val value = this[prop.name] ?: return null
    @Suppress("UNCHECKED_CAST")
    return convertValue(prop.returnType, prop.name, value) as V?
  }

  operator fun set(prop: KProperty<*>, value: Any?) {
    this[prop.name] = value
  }

  fun <V: Any?> remove(prop: KProperty<V>): V? {
    val value = remove(prop.name) ?: return null
    @Suppress("UNCHECKED_CAST")
    return convertValue(prop.returnType, prop.name, value) as V?
  }

  fun keysNotIn(properties: Collection<String>): Set<String> =
    this.keys.minus(properties)

  fun keysNotInProps(properties: Collection<KProperty<*>>): Set<String> =
    this.keysNotIn(properties.map { it.name })

  fun <T: Any?> hasChanged(prop: KProperty<T>, value: () -> T?): Boolean =
    prop in this && this[prop] != value()

  fun <T: Any?> hasChanged(prop: KProperty<T>, from: Any): Boolean =
    prop in this && this[prop] != prop.call(from)

  fun <T: Any?> withChanged(prop: KProperty<T>, value: () -> T?, block: (T?) -> Unit) {
    if(hasChanged(prop, value)) block(this[prop])
  }

  fun <T: Any?> withChanged(prop: KProperty<T>, from: Any, block: (T?) -> Unit) {
    if(hasChanged(prop, from)) block(this[prop])
  }

  fun <T: Any?> withChanged(prop: KProperty<T>, value: () -> Any?): T? {
    return if(hasChanged(prop, value)) this[prop] else null
  }

  fun <T: Any?> withChanged(prop: KProperty<T>, from: Any): T? {
    return if(hasChanged(prop, from)) this[prop] else null
  }

  @Suppress("UNCHECKED_CAST")
  private fun <V : Any> convertValue(type: KType, propName: String, value: V): V? {
    val targetReturnType = type.javaType
    return when(targetReturnType) {
      is Class<*> -> convertClassType(propName, value, targetReturnType)
      is ParameterizedType ->
        convertParameterizedType(propName, value,
          targetReturnType.rawType as Class<*>,
          targetReturnType.actualTypeArguments, type.toString())
      else -> throw PropertyParsingException(listOf(propName), "Unknown return type ${targetReturnType.typeName}")
    } as V
  }

  @Suppress("UNCHECKED_CAST")
  private fun <V : Any> convertClassType(propName: String, value: V?, targetJType: Class<*>): Any? = when {
    value is Int && targetTypeMatch(targetJType, Int::class) -> value
    value is Int && targetTypeMatch(targetJType, Double::class) -> value.toDouble()
    value is Double && targetTypeMatch(targetJType, Double::class) -> value
    value is Boolean && targetTypeMatch(targetJType, Boolean::class) -> value
    value is String && targetTypeMatch(targetJType, String::class) -> value
    value is String && targetJType.kotlin == Instant::class -> {
      try {
        Instant.parse(value)
      } catch(e: DateTimeException) {
        throw PropertyParsingException(listOf(propName), "invalid date/time")
      }
    }
    value is String && targetJType.kotlin == OffsetDateTime::class -> {
      try {
        OffsetDateTime.parse(value)
      } catch(e: DateTimeException) {
        throw PropertyParsingException(listOf(propName), "invalid date/time")
      }
    }
    value is String && targetJType.isEnum -> {
      try {
        EnumHelper.makeEnum(targetJType as Class<Enum<*>>, value)
      } catch(e: Exception) {
        throw PropertyParsingException(listOf(propName), "expected a valid value of type ${targetJType.simpleName}")
      }
    }
    value is Map<*, *> -> {
      try {
        convertMapType(value, targetJType)
      } catch(e: Exception) { when(e) {
        is TypeMismatchException -> throw TypeMismatchException(listOf(propName) + e.fieldPath, e.expectedType, e.receivedType)
        is PropertyParsingException -> throw PropertyParsingException(listOf(propName) + e.fieldPath, e.message ?: "Unable to parse ${targetJType.typeName}")
        else -> throw PropertyParsingException(listOf(propName), e.message ?: "Unable to construct ${targetJType.typeName}")
      }}
    }
    value == null -> value
    value.javaClass == targetJType || value.javaClass.kotlin == targetJType.kotlin
      || targetJType.isAssignableFrom(value.javaClass) -> value
    else -> throw TypeMismatchException(listOf(propName), targetJType.kotlin, value.javaClass)
  }

  @PublishedApi
  internal fun convertMapType(value: Map<*, *>, targetJType: Class<*>): Any? {
    val typeConstructor = targetJType.kotlin.constructors.first()
    return typeConstructor.callBy(typeConstructor.parameters.map {
      val nestedValue = value[it.name]
      if(nestedValue == null) it to null
      else it to convertValue(it.type, it.name!!, nestedValue)
    }.toMap())
  }

  @Suppress("UNCHECKED_CAST")
  private fun <V : Any> convertParameterizedType(propName: String, value: V, targetJType: Class<*>, rawTypeArgs: Array<Type>, targetTypeString: String): Any = when {
    value is List<*> && targetJType.kotlin == List::class -> {
      val typeArg = rawTypeArgs[0]

      fun convertValueList(value: Any, typeArg: Class<*>): Any = (value as List<V?>).map {
        when {
          it != null -> convertClassType(propName, it, typeArg)
          //!typeArg.isKotlinClass() || typeArg.kotlin.defaultType.isMarkedNullable -> null
          targetTypeString.matches(""".*\?>\??$""".toRegex()) -> null  // workaround Kotlin reflection unable to determine nullability of List type
          else -> throw PropertyParsingException(listOf(propName), "Must contain non-null values")
        }
      }

      when(typeArg) {
        is Class<*> -> convertValueList(value, typeArg)
        is WildcardType ->  {
          val bound = typeArg.upperBounds[0]
          @Suppress("RemoveExplicitTypeArguments")
          when(bound) {
            is Class<*> -> convertValueList(value, bound)
            is ParameterizedType -> convertParameterizedType(propName, value,
              bound.rawType as Class<*>,
              arrayOf<Type>(bound), bound.rawType.toString())
            else -> throw PropertyParsingException(listOf(propName), "Unexpected parameterized type argument $typeArg")
          }
        }
        is TypeVariable<*> -> {
          val bound = typeArg.annotatedBounds[0].type
          @Suppress("RemoveExplicitTypeArguments")
          when(bound) {
            is Class<*> -> convertValueList(value, bound)
            is ParameterizedType -> convertParameterizedType(propName, value,
              bound.rawType as Class<*>,
              arrayOf<Type>(bound), bound.rawType.toString())
            else -> throw PropertyParsingException(listOf(propName), "Unexpected parameterized type argument $typeArg")
          }
        }
        is ParameterizedType -> (value as List<V>).map { convertParameterizedType(propName, it,
          typeArg.rawType as Class<*>,
          typeArg.actualTypeArguments, typeArg.rawType.toString()) }
        else -> throw PropertyParsingException(listOf(propName), "Unexpected parameterized type argument $typeArg")
      }
    }
    value is List<*> && targetJType.kotlin == Pair::class -> {
      if(value.filterNotNull().size != 2) {
        throw PropertyParsingException(listOf(propName), "Must have two values")
      }
      Pair(convertClassType(propName, value[0]!!, rawTypeArgs[0] as Class<*>), convertClassType(propName, value[1]!!, rawTypeArgs[1] as Class<*>))
    }
    value is List<*> && targetJType.kotlin == Set::class -> {
      (value as List<V>).map { convertClassType(propName, it, rawTypeArgs[0] as Class<*>) }.toSet()
    }
    value is Set<*> && targetJType.kotlin == Set::class -> {
      (value as Set<V>).map { convertClassType(propName, it, rawTypeArgs[0] as Class<*>) }
    }
    value is Map<*, *> && targetJType.kotlin == Map::class -> PropertyMap(value as Map<String, Any?>)
    else -> throw TypeMismatchException(listOf(propName), targetJType.kotlin, value.javaClass)
  }

  private fun targetTypeMatch(targetJType: Class<*>, kclass: KClass<*>): Boolean =
    targetJType.kotlin == kclass || targetJType == kclass.javaObjectType || targetJType == kclass.javaPrimitiveType
}

fun <K: Any, V: Any?> propMapOf(map: Map<K, V>): PropertyMap = PropertyMap().apply {
  putAll(applyConversions(map))
}

fun <K: Any, V: Any?> propMapOf(vararg pairs: Pair<K, V>): PropertyMap = PropertyMap().apply {
  putAll(applyConversions(pairs.toMap()))
}

inline fun <reified T: Any> propMapOf(type: T): PropertyMap = propMapOf(type.asMap())

inline fun <reified T: Any> T.asMap(): Map<String, Any?> {
  val props = T::class.memberProperties.associateBy { it.name }
  return props.keys.associateWith { props[it]?.get(this) }
}

/** Simple conversion of a typed value map to a JSON value map (used only when creating property maps, not parsing them). */
fun <K: Any, V: Any?> applyConversions(map: Map<K, V>): Map<String, Any?> {
  fun keyToString(key: Any) = (key as? KProperty<*>)?.name ?: key.toString()

  // this kind of sucks but better to fail fast then to silently overwrite a value
  assert(map.keys.distinctBy(::keyToString).size == map.keys.size) { "Map keys overlap after conversion: multiple KProperty's with the same name?" }

  return map
    .mapKeys { keyToString(it.key) }
    .mapValues { convertTypedValueToJson(it.value) }
}

  /** Simple conversion of a typed value to a JSON value (used only when creating property maps, not parsing them). */
@Suppress("UNCHECKED_CAST")
private fun convertTypedValueToJson(value: Any?): Any? = when(value) {
  is List<*> -> value.map(::convertTypedValueToJson)
  is Set<*> -> value.map(::convertTypedValueToJson).toSet()
  is Map<*, *> -> applyConversions(value as Map<Any, Any?>)
  is Enum<*> -> value.name
  is Instant -> value.toString()
  is OffsetDateTime -> value.toString()
  else -> value
}
