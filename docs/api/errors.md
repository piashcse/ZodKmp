# Errors and results

`safeParse` returns a `ZodResult`. Failures contain a structured `ZodError`:

```kotlin
val result = schema.safeParse(input)

when (result) {
    is ZodResult.Success -> println(result.data)
    is ZodResult.Failure -> result.error.issues.forEach { issue ->
        println("${issue.path.renderPath()}: ${issue.message} [${issue.code}]")
    }
}
```

Each `ZodIssue` carries:

- `code`: a `ZodIssueCode` such as `INVALID_TYPE`, `INVALID_FORMAT`, `TOO_SMALL`, `TOO_BIG`,
  `NOT_MULTIPLE_OF`, `INVALID_UNION`, `UNRECOGNIZED_KEYS`, `REQUIRED` or `CUSTOM`.
- `path`: a list of `ZodPathSegment.Property` / `ZodPathSegment.Index` values.
- `message`, plus optional `expected` / `received` / `cause`.

Paths render as `address.city` or `tags[1]`. `ZodError.errors` keeps the historical
path-prefixed string list, and `flatten()` groups issues by top-level field:

```kotlin
val error = (result as ZodResult.Failure).error
val flattened = error.flatten()
// flattened["formErrors"], flattened["fieldErrors"]
```

## Throwing

`parse` and `parseSuspend` throw `ZodValidationException`, which exposes the `ZodError`:

```kotlin
try {
    val user = userSchema.parse(input)
} catch (e: ZodValidationException) {
    println(e.error.flatten())
}
```

## Result helpers

```kotlin
result.getOrNull()
result.getOrThrow()
result.map { transform(it) }
result.fold(onSuccess = { it }, onFailure = { it.issues.size })
result.isSuccess
result.isFailure
```

## JSON

`JsonElement` values can be validated directly; objects and arrays are converted to plain
Kotlin maps and lists:

```kotlin
val result = userSchema.safeParseJson(jsonObject)
```
