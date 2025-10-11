# Transformations

## Transformations

Transform values during validation:

```kotlin
val uppercaseString = Zod.string().transform { it.uppercase() }
val toString = Zod.number().transform { it.toInt().toString() }

// Usage
val result = toString.safeParse(42.5) // Success with "42"
```