# Collections

## Arrays

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

## Tuples

Validate fixed-length arrays with specific types for each position:

```kotlin
val coordinates = Zod.tuple(listOf(Zod.number(), Zod.number()))

// Usage
val result = coordinates.safeParse(listOf(10.0, 20.0))
```

## Records

Validate objects with string keys and uniform value types:

```kotlin
val stringRecord = Zod.record(Zod.string())
val numberRecord = Zod.record(Zod.number())

// Usage
val userData = mapOf("name" to "John", "city" to "NYC")
val result = stringRecord.safeParse(userData)
```