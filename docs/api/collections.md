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

## Sets

Validate sets where every element matches an element schema:

```kotlin
val emailSet = Zod.set(Zod.string().email())
val positiveNumbers = Zod.set(Zod.number().positive())

// Usage
val result = emailSet.safeParse(setOf("a@b.com", "c@d.com")) // Success
```

## Maps

Validate maps with separate key and value schemas:

```kotlin
val scores = Zod.map(Zod.string(), Zod.number())

// Usage
val result = scores.safeParse(mapOf("Alice" to 90.0, "Bob" to 85.0)) // Success
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

### Key-Schema Records

Validate record keys against a schema:

```kotlin
val ipToName = Zod.record(Zod.string().ip(), Zod.string())

val result = ipToName.safeParse(mapOf("192.168.1.1" to "router")) // Success
```

### Loose Records

`looseRecord` validates that all values match, but does not reject keys that
fail the key schema:

```kotlin
val looseIpRecord = Zod.looseRecord(Zod.string().ip(), Zod.string())

// Non-matching keys pass through without error, values are still validated
val result = looseIpRecord.safeParse(mapOf("192.168.1.1" to "router", "hostname" to "home"))
// Success — "hostname" does not match the key schema but is not rejected
```

### Strict Records

`strictRecord` rejects keys that fail the key schema:

```kotlin
val strictIpRecord = Zod.strictRecord(Zod.string().ip(), Zod.string())

val result1 = strictIpRecord.safeParse(mapOf("192.168.1.1" to "router")) // Success
val result2 = strictIpRecord.safeParse(mapOf("hostname" to "home"))      // Failure
```