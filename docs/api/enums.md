# Enums

## String Enums

Validate enum values:

```kotlin
// String enums
val roleSchema = Zod.enum("admin", "user", "guest")

// Using collections
val roles = listOf("admin", "user", "guest")
val roleSchema = Zod.enum(roles)
```