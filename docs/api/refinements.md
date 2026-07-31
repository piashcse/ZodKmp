# Refinements

## Refinements

Add custom validation rules:

```kotlin
val evenNumber = Zod.number().refine({ it.toInt() % 2 == 0 }) { "Number must be even" }
val strongPassword = Zod.string().refine({ it.length >= 8 && it.any { char -> char.isDigit() } }) { "Password must be at least 8 characters with numbers" }
```

## Conditional Validation

```kotlin
// Custom validation based on other fields
val conditionalSchema = Zod.objectSchema<Conditional>({
    string("type", Zod.enum("email", "phone"))
    string("value", Zod.string())
}) { map ->
    Conditional(
        type = map["type"] as String,
        value = map["value"] as String
    )
}.refine({ obj ->
    when (obj.type) {
        "email" -> obj.value.contains("@")
        "phone" -> obj.value.all { it.isDigit() || it == '-' }
        else -> true
    }
}) { "Value must match the selected type" }
```

## Suspend Refinements

Run asynchronous validation rules (e.g. database or network checks). Requires
`kotlinx-coroutines-core`:

```kotlin
val uniqueUsername = Zod.string()
    .min(3)
    .refineSuspend({ username ->
        userRepository.isAvailable(username) // suspend call
    }) { "Username is already taken" }

// Usage from a coroutine
suspend fun check(username: String): Boolean {
    val result = uniqueUsername.safeParseSuspend(username)
    return result is ZodResult.Success
}
```

The sync `parse` and `safeParse` methods throw `IllegalStateException` on a schema
that contains suspend refinements; use `safeParseSuspend`/`parseSuspend` instead.
`safeParseSuspend`/`parseSuspend` are available as defaults on every schema, so
they work with regular schemas too.