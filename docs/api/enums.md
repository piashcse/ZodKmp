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

## Kotlin Enum Classes

Validate against a Kotlin `enum class`. The schema accepts both enum values and
their names, and the result is the enum instance:

```kotlin
enum class Role {
    ADMIN,
    USER,
    GUEST
}

val roleSchema = Zod.enum(Role::class)

// Usage
val result1 = roleSchema.safeParse(Role.ADMIN)   // Success with Role.ADMIN
val result2 = roleSchema.safeParse("ADMIN")      // Success with Role.ADMIN
val result3 = roleSchema.safeParse("admin")      // Failure — names are case-sensitive
val result4 = roleSchema.safeParse("SUPERUSER")  // Failure
```