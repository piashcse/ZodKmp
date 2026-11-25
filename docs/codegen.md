# Code generation (KSP)

`zodkmp-ksp` generates `ZodObjectSchema` definitions for annotated data classes, removing the
manual `{ map -> User(...) }` parser.

## Setup

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.14"
}

dependencies {
    implementation("io.github.piashcse:zodkmp:1.4.0")
    ksp("io.github.piashcse:zodkmp-ksp:1.4.0")
}
```

For Kotlin Multiplatform, add the processor to the target that consumes the models:

```kotlin
dependencies {
    add("kspJvm", "io.github.piashcse:zodkmp-ksp:1.4.0")
    add("kspAndroid", "io.github.piashcse:zodkmp-ksp:1.4.0")
}
```

## Usage

```kotlin
import com.piashcse.zodkmp.ZodModel

@ZodModel(strict = true)
data class User(
    val name: String,
    val age: Int,
    val nickname: String?,
    val tags: List<String>,
)

val result = UserZod.schema.safeParse(input)
```

The processor generates `<ClassName>Zod.schema` in the same package.

## Supported types

- `String`, `Int`, `Long`, `Double`, `Float`, `Boolean`
- enums
- other `@ZodModel` classes
- nullable variants of the above
- `List<T>` of the above

Unsupported types produce a KSP warning and fall back to `Zod.any()`. `@ZodModel(strict = true)`
rejects unknown keys.
