# kpropmap

## Introduction

Kpropmap is a small Kotlin library that combines benefits from untyped and unstructured data like `Map`s, with
the typed and structured data such as provided by data classes. The main class `PropertyMap` implements 
`Map<String, Any?>`, but provides some additional methods to make consuming `Map` content safer and easier by
allowing it's JSON-compatible values to be accessed with types using Kotlin `KProperty` instances (see 
[KProperty](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-property/index.html)).

Properties are stored and matched by name and not by class. The extra meta-data provided by the properties of the data 
class associated with the `PropertyMap` can be used for type checking and automatic conversions / copies of the 
underlying data class or classes.

The primary use case for this is representing part of data classes as a flexible `Map` that represents all or subset of
an immutable data class, while at the same time keeping much of the type information, which can be useful to check and
access the data fluently. This is useful when the data comes from "messy" external systems such as REST interfaces.

Immutable data classes are all-or-nothing. If, say, one needs to implement a REST service that can receive updates to a
subset of the fields of a model data class, delegation to the REST deserialization layer / Jackson will not work, as
there is not enough information received to rebuild the entire data class. Furthermore, there may be complex business
logic associated with processing the information from the client. This type of partial / arbitrary data cannot be easily
represented in a mutable data class structure, nor would that approach be very
[DRY](https://en.wikipedia.org/wiki/Don%27t_repeat_yourself), since many fields will need to be declared at least twice:
once in the immutable structure in primary use as a model, and then again for the value-object layer used to communicate
with other architectural components.

`PropertyMap` can be a useful in-between structure.

### Introspecting Untyped Data via Data Class Properties

The untyped data received from an external system can be introspected and validated against the expected types
given a data class of set of `KProperty`s. This can make validation and parsing logic type-safe and fluent,
and works well with Kotlin's type inference capabilities.

## Caveats

This does make heavy use of reflection, so benchmarking in your environment may be necessary. In addition,
this library currently requires the full 2MB+ `kotlin-reflect` artifact.

## Prerequisites

There are no dependencies except `kotlin-reflect`.

## Getting Started

### Creating a Property Map

The primary class in `kpropmap` is `PropertyMap`. A `PropertyMap` can be created by wrapping an existing
`Map` or `vararg Pair` e.g.:

```kotlin
propMapOf("foo" to "bar")
```

If the keys are `KProperty` instances, the key is automatically converted to the property name. Otherwise, the
`toString()` of the passed value is used as the key. So this would be a more type-safe way to create the same
`PropertyMap` as above:

```kotlin
propMapOf(MyDataClass::foo to "bar")
```

### Using a Property Map

#### Property Maps are Maps

Since it implements `Map<String, Any?>` the standard map interface with `String` keys is always available. However, we
recommend using `KProperty` keys for type-safety. For example, to access a property value corresponding to `myProperty`
in data class `MyDataClass` -- the return value is automatically the correct type, though it is always nullable to
conform to the `Map` interface, which returns `null` for keys not present, as well as for keys present but with a `null`
value:

```kotlin
// p is of type MyDataClass::myProperty (but nullable)
val p = propMap[MyDataClass::myProperty]
```

and standard overloaded operators like `contains` can be used e.g.

```kotlin
val contains = MyDataClass::myProperty in propMap
```

and other standard Map operations such as `remove` also have overloads that accept a `KProperty` and return a value of
the appropriate type (though always nullable, because the contract of `remove` is like `get`: it returns `null` if
either the key was not in the map, or the value was `null`:

```kotlin
val removed = propMap.remove(MyDataClass::myProperty)
```

Since `PropertyMap`'s use Strings as keys, one cannot define a property map like this:

```kotlin
data class Foo(val a: Int)
data class Bar(val a: Int)

propMapOf(Foo::a to 1, Bar::a to 2)
```

This will fail-fast by throwing an `AssertionError` because the two `KProperty` keys have the same `name`, and
thus creating the `PropertyMap` would lose data. Generally this is not a big issue since `PropertyMap`s are most
useful when they contain and represent data from one data class.

#### Property Maps Have Typed Accessors

Property maps accessed with `KProperty` are typed, and will throw useful exceptions when the value type does
not match the property type. For example:

```kotlin
data class MyDataClass(val foo: Int)

// somebody sends us some data
val p = propMapOf("foo" to "bar")

// throws a TypeMismatchException, because f is inferred as an Int? based on the type of `foo`
val f = p[MyDataClass::foo]
```

#### Property Maps Can Be Applied to Existing Data Classes

A `PropertyMap` can be "applied" to an existing data class to support the partial update / patching case. For example:

```kotlin
data class Foo(val a: Int, val b: Int)

val f = Foo(1, 2)
val p = propMapOf("b" to 5)

val f1 = p.applyProps(f) // f1 = Foo(1, 5)
```

#### Property Maps Have Useful Accessors and Extensions

##### Required

The `required` extension can be used to obtain a property that *must* be in the map. It will throw an
`InvalidInputDataException` if the value is not in the map, and ALSO if the value is in the map but `null`:

```kotlin
val p = propMap.required(MyDataClass::myProperty)
// if MyDataClass::myProperty is type T, then p is T
```

The `required` extension behaves like a normal `get` in other respects. For example, it will throw
`TypeMismatchException` like `get` if the field is present, but is the wrong type.
 
The `requiredNullable` extension can be used to obtain a nullable property that *must* be in the map. It will
throw an `InvalidInputDataException` if the value is not in the map, BUT NOT if the value in the map is `null`,
in which case it will return `null`:

```kotlin
val p = propMap.requiredNullable(MyDataClass::myProperty)
// if MyDataClass::myProperty is type T, then p is T?
```

If `MyDataClass::myProperty` is not nullable, `requiredNullable` will throw an `IllegalArgumentException`.

##### @Updateable Field Checks

The `checkRequired` extension can be used with data classes that have `@Updateable` annotations on the class itself,
or on individual fields. If `checkRequired` is called, it will throw an `InvalidInputDataException` if an `Updateable`
property is specified, is non-nullable, but has a null value in the `PropertyMap`. If this call succeeds without
error, one can be comfortable in using `required` in subsequently accessing individual fields.

Similarly, `checkInvalid` can be used to validate that only fields marked `@Updateable` have been provided in the
`PropertyMap`. Any other fields present will cause a `InvalidInputDataException`. This can be a useful check before
[applying](#property-maps-can-be-applied-to-existing-data-classes) a `PropertyMap` to a data class.

##### Has Changed

The `hasChanged` method allows one to determine whether the `PropertyMap` contains a value that is different than the
one in an existing data class (or any arbitrary value obtained via a lambda).

##### With Change

The `withChanged` methods allow one to execute code, or return a changed value, only if the `PropertyMap` contains a
value that is different than the one in an existing data class (or any arbitrary value obtained via a lambda).

##### Extraneous Keys

The `PropertyMap` may contain extraneous keys that are not mappable to a particular data class. The `keysNotIn` and
`keysNotInProps` methods allow for easy identification of such data. For example:

```kotlin
data class MyDataClass(val foo: Int)
val p = propMapOf(
  "whatever" to 1,
  "foo" to 2
)
val keys = p.keysNotInProps(MyDataClass::class.memberProperties)
// keys is list("whatever")
```

#### Property Maps Can Be Deserialized to Data Classes

If all the properties are available, `PropertyMap`'s can be deserialized to data classes. The `deserialize` extension
can do this given a target type e.g.:

```kotlin
val foo = propMap.deserialize<Foo>()
```

Consider Jackson (with the kotlin module) for this type of wholesale deserialization.

#### Property Maps Can Be Nested

Property maps can be nested and can thus map to and from nested data classes e.g.:

```kotlin
data class Foo(val a: Int, val b: Bar)
data class Bar(val a: Int)
val l = propMapOf(
  Foo::a to 1, 
  Foo::b to propMapOf(
    Bar:a to 2
  )
)
```

Partially supplied nested properties can also be applied to existing data structures as
[above](#property-maps-can-be-applied-to-existing-data-classes).

If all the properties are available in the `PropertyMap`, then the entire structure can be deserialized as
[above](#property-maps-can-be-deserialized-to-data-classes).

#### Some Useful Conversions are Available

Some additional useful conventions / conversions automatically performed. More may be added to this list as use cases
arise.

##### Pairs are 2-element Lists

A `PropertyMap` containing a 2-element `List` as a value can be obtained as a `Pair` when using a `PropertyMap` e.g.:

```kotlin
data class Foo(val l: Pair<String, Int>)
val p = propMapOf(Foo::l to listOf("answer", 42))
val pair = p[Foo::l] // pair is Pair<String, Int>("answer", 42)
```

##### Instant and OffsetDateTime are Supported

Conversions for `Instant` and `OffsetDateTime` are supported.

#### Exceptions Provide Useful Information

Because `PropertyMap` is generally used to read externally produced data, exceptional conditions are not unusual. For
example, non-nullable fields may be `null`, field values may not be the correct type, and so on.

When a subclass of `PropertyMapException` occurs, the `Exception` will contain useful information about why and where
the Exception occurred. This information is suitable for producing good descriptive error responses e.g. a `400 Bad
Request` with details about which fields were problematic.

## Built With

* [Kotlin](https://kotlinlang.org/)

## Contributing

Be nice. Submit issues. Submit pull requests.

## Authors

* **Raman Gupta** - *Initial work* - [LinkedIn](https://www.linkedin.com/in/rocketraman/)

## Contributors

TODO()

## TODOs

* Make it more modular e.g. allow users to register additional conversions, make existing conversions come from
built-in modules

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
