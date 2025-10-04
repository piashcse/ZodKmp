# ZodKmp: Kotlin Multiplatform Validation


[![Maven Central](https://img.shields.io/maven-central/v/io.github.piashcse/zodkmp.svg)](https://search.maven.org/artifact/io.github.piashcse/zodkmp)
[![Kotlin Version](https://img.shields.io/badge/kotlin-2.2.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
![badge-Android](https://img.shields.io/badge/Platform-Android-brightgreen)
![badge-iOS](https://img.shields.io/badge/Platform-iOS-lightgray)
![badge-desktop](http://img.shields.io/badge/Platform-Desktop-4D76CD.svg?style=flat)
![badge-web](https://img.shields.io/badge/Platform-Web-blueviolet.svg?style=flat)
[![License](https://img.shields.io/github/license/colinhacks/zod)](LICENSE)
<a href="https://github.com/piashcse"><img alt="License" src="https://img.shields.io/static/v1?label=GitHub&message=piashcse&color=C51162"/></a>

ZodKmp is a Kotlin Multiplatform implementation of the popular [Zod](https://zod.dev/) TypeScript validation library. It provides a declarative, type-safe way to validate data in your Kotlin Multiplatform projects.


## Platform Support

ZodKmp supports the following platforms:

- Android (JVM)
- iOS (Native)
- JVM
- JS (JavaScript)
- Native (Linux, Windows, macOS)


## Installation

### Gradle

Add the following to your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.piashcse:zodkmp:1.1.0")
            }
        }
    }
}
```

### Version Catalog (libs.versions.toml)

```toml
[versions]
zodkmp = "1.1.0"

[libraries]
zodkmp = { module = "io.github.piashcse:zodkmp", version.ref = "zodkmp" }
```


## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
  - [Primitives](#primitives)
  - [Literals](#literals)
  - [Strings](#strings)
  - [Numbers](#numbers)
  - [Booleans](#booleans)
  - [Dates](#dates)
  - [Arrays](#arrays)
  - [Objects](#objects)
  - [Unions](#unions)
  - [Enums](#enums)
  - [Tuples](#tuples)
  - [Records](#records)
  - [Intersections](#intersections)
  - [Nullables](#nullables)
  - [Optionals](#optionals)
  - [Transformations](#transformations)
  - [Refinements](#refinements)
  - [Defaults](#defaults)
- [Advanced Usage](#advanced-usage)
- [Platform Support](#platform-support)
- [Contributing](#contributing)
- [License](#license)

## Features

- ✅ **Declarative Schema Definition** - Define validation rules upfront
- ✅ **Type Inference** - Automatic type inference from schemas
- ✅ **Immutable Architecture** - Immutable schemas that return new instances
- ✅ **Kotlin Multiplatform** - Works on Android, iOS, and other Kotlin targets
- ✅ **Comprehensive API** - Supports all major Zod validation features
- ✅ **Extensible** - Easy to extend with custom validations
- ✅ **Zero Dependencies** - Lightweight with minimal footprint
- ✅ **Excellent Error Messages** - Detailed, customizable error reporting
- 
## Getting Started

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

## API Reference

### Primitives

#### String
Validate string values with various constraints:

```kotlin
val stringSchema = Zod.string()

// With constraints
val constrainedString = Zod.string()
    .min(5, "String must be at least 5 characters")
    .max(100, "String must be at most 100 characters")
    .length(10, "String must be exactly 10 characters")
    .email("Must be a valid email address")
    .url("Must be a valid URL")
    .regex(Regex("^[A-Za-z]+$"), "Must contain only letters")
    .startsWith("Hello", "Must start with 'Hello'")
    .endsWith("World", "Must end with 'World'")
    .includes("test", "Must contain 'test'")
    .toLowerCase()
    .toUpperCase()
    .trim()
```

#### Number
Validate numeric values:

```kotlin
val numberSchema = Zod.number()

// With constraints
val constrainedNumber = Zod.number()
    .gt(0, "Must be greater than 0")
    .gte(5, "Must be greater than or equal to 5")
    .lt(100, "Must be less than 100")
    .lte(50, "Must be less than or equal to 50")
    .min(10, "Must be at least 10")
    .max(90, "Must be at most 90")
    .int("Must be an integer")
    .positive("Must be positive")
    .negative("Must be negative")
    .nonPositive("Must be non-positive")
    .nonNegative("Must be non-negative")
    .multipleOf(5, "Must be a multiple of 5")
```

#### Boolean
Validate boolean values:

```kotlin
val booleanSchema = Zod.boolean()
```

#### Date
Validate date values:

```kotlin
val dateSchema = Zod.date()

// With constraints
val constrainedDate = Zod.date()
    .min(LocalDateTime(2020, 1, 1, 0, 0), "Date must be after 2020")
    .max(LocalDateTime(2030, 12, 31, 23, 59), "Date must be before 2030")
```

#### Null & Undefined
Validate null and undefined values:

```kotlin
val nullSchema = Zod.null()
val undefinedSchema = Zod.undefined()
```

### Literals

Validate exact literal values:

```kotlin
val literalSchema = Zod.literal("admin")
val numberLiteral = Zod.literal(42)
val booleanLiteral = Zod.literal(true)
```

### Arrays

Validate array values with constraints:

```kotlin
val stringArray = Zod.array(Zod.string())

// With constraints
val constrainedArray = Zod.array(Zod.string())
    .min(2, "Array must have at least 2 elements")
    .max(10, "Array must have at most 10 elements")
    .length(5, "Array must have exactly 5 elements")
    .nonempty("Array must not be empty")
```

### Objects

Validate complex objects:

```kotlin
val userSchema = Zod.objectSchema<User>({
    string("name", Zod.string().min(2))
    string("email", Zod.string().email())
    number("age", Zod.number().min(0).max(120))
    field("isActive", Zod.boolean().default(true))
}) { map ->
    User(
        name = map["name"] as String,
        email = map["email"] as String,
        age = (map["age"] as Number).toDouble(),
        isActive = map["isActive"] as? Boolean ?: true
    )
}

// Strict objects (reject unknown keys)
val strictUserSchema = Zod.objectSchema<User>({
    string("name", Zod.string().min(2))
    string("email", Zod.string().email())
}) { map ->
    User(
        name = map["name"] as String,
        email = map["email"] as String
    )
}.strict()
```

### Enums

Validate enum values:

```kotlin
// String enums
val roleSchema = Zod.enum("admin", "user", "guest")

// Using collections
val roles = listOf("admin", "user", "guest")
val roleSchema = Zod.enum(roles)
```

### Unions

Validate values that match any of multiple schemas:

```kotlin
val stringOrNumber = Zod.union(Zod.string(), Zod.number())
val complexUnion = Zod.union(
    Zod.string(),
    Zod.number(),
    Zod.objectSchema<Point>({
        number("x", Zod.number())
        number("y", Zod.number())
    }) { map ->
        Point(
            x = (map["x"] as Number).toDouble(),
            y = (map["y"] as Number).toDouble()
        )
    }
)
```

### Tuples

Validate fixed-length arrays with specific types for each position:

```kotlin
val coordinates = Zod.tuple(listOf(Zod.number(), Zod.number()))

// Usage
val result = coordinates.safeParse(listOf(10.0, 20.0))
```

### Records

Validate objects with string keys and uniform value types:

```kotlin
val stringRecord = Zod.record(Zod.string())
val numberRecord = Zod.record(Zod.number())

// Usage
val userData = mapOf("name" to "John", "city" to "NYC")
val result = stringRecord.safeParse(userData)
```

### Intersections

Validate values that must satisfy multiple schemas:

```kotlin
val personSchema = Zod.objectSchema<Person>({
    string("name", Zod.string())
}) { map ->
    Person(name = map["name"] as String)
}

val employeeSchema = Zod.objectSchema<Employee>({
    string("employeeId", Zod.string())
}) { map ->
    Employee(employeeId = map["employeeId"] as String)
}

val personEmployeeSchema = Zod.intersection(personSchema, employeeSchema)
```

### Nullables

Mark schemas as accepting null values:

```kotlin
val nullableString = Zod.string().nullable()

// Usage
val result1 = nullableString.safeParse("hello") // Success
val result2 = nullableString.safeParse(null)   // Success
val result3 = nullableString.safeParse(42)       // Failure
```

### Optionals

Mark schemas as accepting undefined values:

```kotlin
val optionalString = Zod.string().optional()

// Usage
val result1 = optionalString.safeParse("hello") // Success
val result2 = optionalString.safeParse(null)     // Success (undefined)
```

### Transformations

Transform values during validation:

```kotlin
val uppercaseString = Zod.string().transform { it.uppercase() }
val toString = Zod.number().transform { it.toInt().toString() }

// Usage
val result = toString.safeParse(42.5) // Success with "42"
```

### Refinements

Add custom validation rules:

```kotlin
val evenNumber = Zod.number().refine({ it.toInt() % 2 == 0 }) { "Number must be even" }
val strongPassword = Zod.string().refine({ it.length >= 8 && it.any { char -> char.isDigit() } }) { "Password must be at least 8 characters with numbers" }
```

### Defaults

Provide default values for schemas:

```kotlin
val stringWithDefault = Zod.string().default("unknown")
val numberWithDefault = Zod.number().default { 0.0 }

// Usage
val result1 = stringWithDefault.safeParse(null)     // Success with "unknown"
val result2 = stringWithDefault.safeParse("hello")  // Success with "hello"
```

## Advanced Usage

### Nested Object Validation

```kotlin
val addressSchema = Zod.objectSchema<Address>({
    string("street", Zod.string().min(5))
    string("city", Zod.string().min(2))
    string("zipCode", Zod.string().regex(Regex("\\d{5}"))
}) { map ->
    Address(
        street = map["street"] as String,
        city = map["city"] as String,
        zipCode = map["zipCode"] as String
    )
}

val userWithAddressSchema = Zod.objectSchema<UserWithAddress>({
    string("name", Zod.string().min(2))
    field("address", addressSchema)
}) { map ->
    UserWithAddress(
        name = map["name"] as String,
        address = map["address"] as Address
    )
}
```

### Conditional Validation

```kotlin
// Custom validation based on other fields
val conditionalSchema = Zod.objectSchema<Conditional>({
    string("type", Zod.enum("email", "phone"))
    string("value", Zod.string())
}) { map ->
    Conditional(
        type = map["type"] as String,
        value = map["value"] as String
    )
}.refine({ obj ->
    when (obj.type) {
        "email" -> obj.value.contains("@")
        "phone" -> obj.value.all { it.isDigit() || it == '-' }
        else -> true
    }
}) { "Value must match the selected type" }
```

### Error Handling

ZodKmp provides detailed error information:

```kotlin
val userSchema = Zod.objectSchema<User>({
    string("name", Zod.string().min(2))
    string("email", Zod.string().email())
}) { map ->
    User(
        name = map["name"] as String,
        email = map["email"] as String
    )
}

val result = userSchema.safeParse(mapOf("name" to "A", "email" to "invalid-email"))

when (result) {
    is ZodResult.Success -> {
        println("Valid data: ${result.data}")
    }
    is ZodResult.Failure -> {
        result.error.errors.forEach { error ->
            println("Validation error: $error")
        }
    }
}
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
## 👨 Developed By

<a href="https://twitter.com/piashcse" target="_blank">
  <img src="https://avatars.githubusercontent.com/piashcse" width="90" align="left">
</a>

**Mehedi Hassan Piash**

[![Twitter](https://img.shields.io/badge/-Twitter-1DA1F2?logo=x&logoColor=white&style=for-the-badge)](https://twitter.com/piashcse)
[![Medium](https://img.shields.io/badge/-Medium-00AB6C?logo=medium&logoColor=white&style=for-the-badge)](https://medium.com/@piashcse)
[![Linkedin](https://img.shields.io/badge/-LinkedIn-0077B5?logo=linkedin&logoColor=white&style=for-the-badge)](https://www.linkedin.com/in/piashcse/)
[![Web](https://img.shields.io/badge/-Web-0073E6?logo=appveyor&logoColor=white&style=for-the-badge)](https://piashcse.github.io/)
[![Blog](https://img.shields.io/badge/-Blog-0077B5?logo=readme&logoColor=white&style=for-the-badge)](https://piashcse.blogspot.com)

## License

```
MIT License

Copyright (c) 2025 Mehedi Hassan Piash

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
