# Transformations

## Transformations

Transform values during validation:

```kotlin
val uppercaseString = Zod.string().transform { it.uppercase() }
val toString = Zod.number().transform { it.toInt().toString() }

// Usage
val result = toString.safeParse(42.5) // Success with "42"
```

## Preprocess

Transform the input before validating it with the wrapped schema:

```kotlin
val trimAndValidate = Zod.string().trim().preprocess { value ->
    (value as? String)?.trim()
}

val numFromString = Zod.preprocess({ (it as? String)?.toDoubleOrNull() }, Zod.number())
val result = numFromString.safeParse("42.5") // Success with 42.5
```

`preprocess` is also available as an extension on any schema.

## Catch

Return a fallback value when validation fails:

```kotlin
val withDefault = Zod.string().catch("unknown")

// Or compute the fallback from the invalid input
val lengthOrDefault = Zod.string().catch { input -> input?.toString()?.length ?: 0 }

val result1 = withDefault.safeParse(null)    // Success with "unknown"
val result2 = withDefault.safeParse("hello") // Success with "hello"
```

## Pipe

Validate the output of one schema with another schema:

```kotlin
val stringToNumber = Zod.pipe(Zod.string(), Zod.number())
// Or: Zod.string().pipe(Zod.number())

val result = stringToNumber.safeParse("42.5") // Success with 42.5
```

## Describe & Meta

Attach a description or metadata to a schema without changing validation:

```kotlin
val userSchema = Zod.string()
    .describe("The user's full name")
    .meta(mapOf("kind" to "name", "required" to true))

// Access the metadata
val describe = userSchema as ZodDescribe<*>
println(describe.description) // "The user's full name"
println(describe.metadata)    // {kind=name, required=true}
```