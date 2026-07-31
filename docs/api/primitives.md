# Primitives

## String

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

### String Formats

Validate common string formats:

```kotlin
val uuidSchema = Zod.string().uuid()
val ipv4Schema = Zod.string().ipv4()
val ipv6Schema = Zod.string().ipv6()
val ipSchema = Zod.string().ip()          // accepts IPv4 or IPv6
val emojiSchema = Zod.string().emoji()
val cuidSchema = Zod.string().cuid()
val cuid2Schema = Zod.string().cuid2()
val ulidSchema = Zod.string().ulid()
val nanoidSchema = Zod.string().nanoid()
val datetimeSchema = Zod.string().datetime()   // ISO-8601
val base64Schema = Zod.string().base64()
val base64urlSchema = Zod.string().base64url()

// Usage
val valid = uuidSchema.safeParse("123e4567-e89b-12d3-a456-426614174000") // Success
val invalid = uuidSchema.safeParse("not-a-uuid")                          // Failure
```

## Number

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
    .finite("Must be a finite number")
```

## Long

Validate 64-bit integers (the Kotlin analog of Zod's `bigint`):

```kotlin
val longSchema = Zod.long()
val positiveId = Zod.long().positive().max(1_000_000)

// Usage (accepts Long, integral numbers and numeric strings)
val result1 = longSchema.safeParse(9223372036854775807L) // Success
val result2 = longSchema.safeParse(42)                    // Success
val result3 = longSchema.safeParse("7")                   // Success
val result4 = longSchema.safeParse(3.14)                  // Failure
```

## Boolean

Validate boolean values:

```kotlin
val booleanSchema = Zod.boolean()
```

## Date

Validate date values:

```kotlin
val dateSchema = Zod.date()

// With constraints
val constrainedDate = Zod.date()
    .min(LocalDateTime(2020, 1, 1, 0, 0), "Date must be after 2020")
    .max(LocalDateTime(2030, 12, 31, 23, 59), "Date must be before 2030")
```

## Null & Undefined

Validate null and undefined values:

```kotlin
val nullSchema = Zod.nullType()
val undefinedSchema = Zod.undefined()
```

## Literals

Validate exact literal values:

```kotlin
val literalSchema = Zod.literal("admin")
val numberLiteral = Zod.literal(42)
val booleanLiteral = Zod.literal(true)
```

Literal accepts multiple values and returns a union of literals. A single value
returns a plain `ZodLiteral`, and no values returns `ZodNever`:

```kotlin
val statusLiteral = Zod.literal("draft", "published", "archived")
val result1 = statusLiteral.safeParse("published") // Success
val result2 = statusLiteral.safeParse("deleted")   // Failure
```

## Any

Accepts any value without validation:

```kotlin
val anySchema = Zod.any()
val result = anySchema.safeParse(mapOf("anything" to listOf(1, 2, 3))) // Success
```

## Unknown

Accepts any value, but the result stays typed as `Any?`:

```kotlin
val unknownSchema = Zod.unknown()
val result = unknownSchema.safeParse("whatever") // Success
```

## Never

Rejects every value (useful to mark impossible branches):

```kotlin
val neverSchema = Zod.never()
val result = neverSchema.safeParse(42) // Failure
```

## Void

Accepts null values and returns `Unit` (mirrors Zod's `z.void()` which accepts
`undefined`):

```kotlin
val voidSchema = Zod.void()
val result = voidSchema.safeParse(null) // Success with Unit
```

## InstanceOf

Validate that a value is an instance of a given class (reified, no reflection):

```kotlin
val dateSchema = Zod.instanceOf<Date>()
val urlSchema = Zod.instanceOf<URL>()

// Usage
val result1 = dateSchema.safeParse(Date())     // Success
val result2 = dateSchema.safeParse("2026-01-01") // Failure
```

## Nullables

Mark schemas as accepting null values:

```kotlin
val nullableString = Zod.string().nullable()

// Usage
val result1 = nullableString.safeParse("hello") // Success
val result2 = nullableString.safeParse(null)   // Success
val result3 = nullableString.safeParse(42)       // Failure
```

## Optionals

Mark schemas as accepting undefined values:

```kotlin
val optionalString = Zod.string().optional()

// Usage
val result1 = optionalString.safeParse("hello") // Success
val result2 = optionalString.safeParse(null)     // Success (undefined)
```

## Defaults

Provide default values for schemas:

```kotlin
val stringWithDefault = Zod.string().default("unknown")
val numberWithDefault = Zod.number().default { 0.0 }

// Usage
val result1 = stringWithDefault.safeParse(null)     // Success with "unknown"
val result2 = stringWithDefault.safeParse("hello")  // Success with "hello"
```