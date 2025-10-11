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
val nullSchema = Zod.null()
val undefinedSchema = Zod.undefined()
```

## Literals

Validate exact literal values:

```kotlin
val literalSchema = Zod.literal("admin")
val numberLiteral = Zod.literal(42)
val booleanLiteral = Zod.literal(true)
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