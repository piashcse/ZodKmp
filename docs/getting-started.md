# Getting Started

## Installation

### Gradle

Add the following to your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.piashcse:zodkmp:1.2.0")
            }
        }
    }
}
```

### Version Catalog (libs.versions.toml)

```toml
[versions]
zodkmp = "1.2.0"

[libraries]
zodkmp = { module = "io.github.piashcse:zodkmp", version.ref = "zodkmp" }
```

## Basic Usage

ZodKmp allows you to define validation schemas and use them to validate data:

```kotlin
import com.piashcse.zodkmp.Zod

// Define a schema
val userSchema = Zod.objectSchema<User>({
    string("name", Zod.string().min(2))
    string("email", Zod.string().email())
    number("age", Zod.number().min(0).max(120))
}) { map ->
    User(
        name = map["name"] as String,
        email = map["email"] as String,
        age = (map["age"] as Number).toDouble()
    )
}

// Use the schema
val userData = mapOf(
    "name" to "John Doe",
    "email" to "john@example.com",
    "age" to 30.0
)

val result = userSchema.safeParse(userData)
when (result) {
    is ZodResult.Success -> println("Valid user: ${result.data}")
    is ZodResult.Failure -> println("Validation errors: ${result.error.errors}")
}
```