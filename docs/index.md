---
hide:
  - navigation
---

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

## Features

- ✅ **Declarative Schema Definition** - Define validation rules upfront
- ✅ **Type Inference** - Automatic type inference from schemas
- ✅ **Immutable Architecture** - Immutable schemas that return new instances
- ✅ **Kotlin Multiplatform** - Works on Android, iOS, and other Kotlin targets
- ✅ **Comprehensive API** - Supports all major Zod validation features
- ✅ **Extensible** - Easy to extend with custom validations
- ✅ **Zero Dependencies** - Lightweight with minimal footprint
- ✅ **Excellent Error Messages** - Detailed, customizable error reporting

## Getting Started

### Installation

#### Gradle

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

#### Version Catalog (libs.versions.toml)

```toml
[versions]
zodkmp = "1.2.0"

[libraries]
zodkmp = { module = "io.github.piashcse:zodkmp", version.ref = "zodkmp" }
```

### Basic Usage

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