# ZodKmp: Kotlin Multiplatform Validation

[![Maven Central](https://img.shields.io/maven-central/v/io.github.piashcse/zodkmp.svg)](https://central.sonatype.com/artifact/io.github.piashcse/zodkmp)
[![Kotlin Version](https://img.shields.io/badge/kotlin-2.4.21-blue.svg?logo=kotlin)](https://kotlinlang.org)
![Android](https://img.shields.io/badge/Platform-Android-brightgreen)
![iOS](https://img.shields.io/badge/Platform-iOS-lightgray)
![JVM](https://img.shields.io/badge/Platform-JVM-4D76CD)
![Web](https://img.shields.io/badge/Platform-Web-blueviolet)
[![License](https://img.shields.io/github/license/piashcse/zodkmp)](LICENSE)

ZodKmp is a Kotlin Multiplatform implementation of the [Zod](https://zod.dev/) validation
library. It provides declarative, type-safe schemas that run on Android, iOS, JVM, JS, Wasm
and native targets.

Full documentation: <https://piashcse.github.io/ZodKmp/>

## Platform support

Android, iOS, JVM/desktop, JS (browser and Node.js), Wasm (browser), Linux, macOS and Windows.

## Installation

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.piashcse:zodkmp:2.0.0")
        }
    }
}
```

Version catalog:

```toml
[versions]
zodkmp = "2.0.0"

[libraries]
zodkmp = { module = "io.github.piashcse:zodkmp", version.ref = "zodkmp" }
```

## Quick start

```kotlin
import com.piashcse.zodkmp.*

data class User(val name: String, val email: String, val age: Double, val isActive: Boolean)

val userSchema = Zod.objectSchema<User>({
    string("name", Zod.string().min(2))
    string("email", Zod.string().email())
    number("age", Zod.number().min(0.0).max(120.0).int())
    field("isActive", Zod.boolean().default(true))
}) { map ->
    User(
        name = map["name"] as String,
        email = map["email"] as String,
        age = map["age"] as Double,
        isActive = map["isActive"] as Boolean
    )
}

when (val result = userSchema.safeParse(input)) {
    is ZodResult.Success -> println(result.data)
    is ZodResult.Failure -> result.error.issues.forEach { issue ->
        println("${issue.path.renderPath()}: ${issue.message} (${issue.code})")
    }
}
```

`parse` throws `ZodValidationException` instead of a plain `IllegalArgumentException`:

```kotlin
val user = try {
    userSchema.parse(input)
} catch (e: ZodValidationException) {
    println(e.error.flatten()) // formErrors + fieldErrors
    throw e
}
```

## API reference

### Primitives

```kotlin
Zod.string()
Zod.number()
Zod.boolean()
Zod.date()            // kotlinx.datetime.LocalDateTime
Zod.long()
Zod.nullType()        // or Zod.`null`()
Zod.undefined()
Zod.any()
Zod.unknown()
Zod.never()
Zod.void()
```

### Strings

```kotlin
Zod.string()
    .min(2)
    .max(100)
    .email()
    .url()
    .uuid()
    .ipv4()
    .ipv6()
    .ip()
    .emoji()
    .cuid()
    .cuid2()          // exactly 24 chars by default
    .ulid()
    .nanoid()         // exactly 21 chars by default
    .base64()
    .base64url()
    .datetime()       // ISO-8601 date-time, offsets allowed, date-only rejected
    .regex(Regex("^[A-Z]+$"))
    .startsWith("A")
    .endsWith("Z")
    .includes("mid")
    .trim()
    .toLowerCase()
    .toUpperCase()
    .refine({ !it.contains(" ") }) { "must not contain spaces" }
```

Checks run in declaration order, so `min(4).trim()` validates the untrimmed value while
`trim().min(4)` validates the trimmed value. Lengths are counted in Unicode code points.
`.regex()` matches anywhere in the string, like Zod.

### Numbers and coercion

```kotlin
Zod.number()
    .min(0.0)
    .max(100.0)
    .gt(0.0)
    .gte(1.0)
    .lt(100.0)
    .lte(99.0)
    .int()
    .positive()
    .negative()
    .nonPositive()
    .nonNegative()
    .multipleOf(0.5)
    .finite()
```

Numeric strings are **not** accepted implicitly:

```kotlin
Zod.number().safeParse("42")      // Failure
Zod.coerce.number().parse("42")   // 42.0
```

Coercion is opt-in for numbers, booleans, longs and dates: `Zod.coerce.number()`,
`Zod.coerce.boolean()`, `Zod.coerce.long()`, `Zod.coerce.date()`.

### Arrays, sets, tuples and maps

```kotlin
Zod.array(Zod.string()).min(1).max(10).unique()
Zod.set(Zod.number()).min(1)                 // requires a Set, lists are rejected
Zod.tuple(Zod.string(), Zod.number())        // exactly two positions
Zod.map(Zod.string(), Zod.number())          // typed keys and values
Zod.record(Zod.number())                     // Map<String, Double>
Zod.record(Zod.string().min(1), Zod.number())// strict key validation
Zod.looseRecord(Zod.string(), Zod.number())  // keys are not rejected
```

Primitive arrays (`IntArray`, `DoubleArray`, ...) are accepted by `Zod.array`.

### Objects

```kotlin
val schema = Zod.objectSchema<User>({
    string("name", Zod.string().min(2))
    optionalString("nickname")
    field("address", addressSchema)
}) { map -> /* construct User */ }

schema.strict()       // reject unknown keys
schema.strip()        // drop unknown keys (default)
schema.passthrough()  // keep unknown keys

schema.pick("name")
schema.omit("nickname")
schema.partial()      // all fields optional
schema.deepPartial()  // nested objects optional too, collection constraints preserved
schema.merge(other)   // later keys win
schema.extend(other)
schema and other
```

The last call to `strict`, `strip` or `passthrough` wins. Missing required properties produce a
`REQUIRED` issue with the property path.

### Unions and intersections

```kotlin
val id = Zod.union(Zod.string(), Zod.number())

val a = Zod.objectSchema<Map<String, Any?>>({ string("a") }) { it }
val b = Zod.objectSchema<Map<String, Any?>>({ string("b") }) { it }
val merged = Zod.intersection(a, b) // single merged object schema
```

For non-object schemas, `Zod.intersection(left, right)` returns a `Pair<T, U>`.

### Discriminated unions

```kotlin
sealed interface Animal {
    data class Dog(val name: String, val goodBoy: Boolean) : Animal
    data class Cat(val name: String, val lives: Double) : Animal
}

val dog = Zod.objectSchema<Animal.Dog>({
    field("type", Zod.literal("dog"))
    string("name")
    boolean("goodBoy")
}) { map -> Animal.Dog(map["name"] as String, map["goodBoy"] as Boolean) }

val cat = Zod.objectSchema<Animal.Cat>({
    field("type", Zod.literal("cat"))
    string("name")
    number("lives")
}) { map -> Animal.Cat(map["name"] as String, map["lives"] as Double) }

val animal = Zod.discriminatedUnion<Animal>("type", dog, cat)
```

Every option must declare a `ZodLiteral` for the discriminator, and discriminator values must
be unique. Unknown discriminators fail immediately.

### Enums and literals

```kotlin
Zod.enum("admin", "user", "guest")
Zod.enum(listOf("admin", "user"))
Zod.enum(Role::class)              // accepts enum names as strings
Zod.literal("admin")
Zod.literal(1, 2, 3)               // union of literals
```

Numeric literals match across Kotlin numeric types (`Zod.literal(1)` accepts `1L`).

### Optional, nullable, defaults

```kotlin
Zod.string().optional()            // missing property or null -> null
Zod.string().nullable()            // null allowed, missing property still required
Zod.string().default("unknown")    // default for missing properties
Zod.string().optional().default("fallback") // explicit null stays null
```

### Transforms, refinements and pipes

```kotlin
Zod.string().trim().transform { it.uppercase() }
Zod.number().refine({ it % 2.0 == 0.0 }) { "must be even" } // keeps ZodNumber type
Zod.string().refineSuspend({ api.isAvailable(it) })          // use safeParseSuspend
Zod.string().pipe(Zod.coerce.number())
Zod.preprocess({ (it as? String)?.trim() }, Zod.string().min(1))
Zod.string().describe("Display name")
```

`refine` on string, number, boolean, array, set, object and date schemas returns the same
concrete schema type, so further checks can be chained.

### Recursive schemas

```kotlin
lateinit var node: ZodSchema<Map<String, Any?>>
node = Zod.lazy {
    Zod.objectSchema<Map<String, Any?>>({
        string("name")
        field("children", Zod.array(node))
    }) { it }
}
```

### Code generation (KSP)

The optional `zodkmp-ksp` processor generates schemas and parsers for data classes:

```kotlin
// build.gradle.kts
plugins {
    id("com.google.devtools.ksp") version "2.3.14"
}

dependencies {
    implementation("io.github.piashcse:zodkmp:2.0.0")
    ksp("io.github.piashcse:zodkmp-ksp:2.0.0")
}
```

```kotlin
import com.piashcse.zodkmp.ZodModel

@ZodModel(strict = true)
data class User(val name: String, val age: Int, val nickname: String?)

val result = UserZod.schema.safeParse(input)
```

Supported types: `String`, `Int`, `Long`, `Double`, `Float`, `Boolean`, enums, other
`@ZodModel` classes, nullable variants and `List<T>`. Unknown types fall back to `Zod.any()`
and emit a KSP warning. Strict mode rejects unknown keys.

### JSON interop

```kotlin
val json = buildJsonObject {
    put("name", "Ada")
    put("age", 30)
}

val result = userSchema.safeParseJson(json) // JsonObject/JsonArray become plain Kotlin values
```

### Errors and results

```kotlin
val result = schema.safeParse(input)

if (result is ZodResult.Failure) {
    result.error.issues.forEach { issue ->
        println("${issue.path.renderPath()}: ${issue.message} [${issue.code}]")
    }
    result.error.errors       // List<String>, path-prefixed messages
    result.error.flatten()    // formErrors / fieldErrors
}

schema.safeParse(input).getOrNull()
schema.safeParse(input).getOrThrow()
schema.safeParse(input).map { /* ... */ }
schema.safeParse(input).fold(onSuccess = { it }, onFailure = { it.errors.size })
```

Paths are structured (`ZodPathSegment.Property` / `ZodPathSegment.Index`) and render as
`address.city` or `tags[1]`. Use `ZodConfig.errorMap = { issue -> ... }` to localize messages.

`toJsonSchema()` converts a schema to a structural JSON Schema object:

```kotlin
val jsonSchema = userSchema.toJsonSchema()
```

## Building

The build requires JDK 21. Gradle 9.9.2 provisions the toolchain automatically through the
Foojay resolver.

```bash
./gradlew :zodkmp:desktopTest     # common tests on the JVM
./gradlew :zodkmp:apiCheck        # binary compatibility check
./gradlew :zodkmp:apiDump         # update api/*.api after intentional API changes
./gradlew :zodkmp:dokkaGenerateHtml
./gradlew :zodkmp:koverHtmlReport
```

## Contributing

See [docs/contributing.md](docs/contributing.md). Please run the tests and `apiDump` before
opening a pull request.

## License

MIT — see [LICENSE](LICENSE). Copyright (c) 2025 Mehedi Hassan Piash.
