# Advanced Usage

## Error Handling

ZodKmp provides detailed error information:

```kotlin
val userSchema = Zod.objectSchema<User>({
    string("name", Zod.string().min(2))
    string("email", Zod.string().email())
}) { map ->
    User(
        name = map["name"] as String,
        email = map["email"] as String
    )
}

val result = userSchema.safeParse(mapOf("name" to "A", "email" to "invalid-email"))

when (result) {
    is ZodResult.Success -> {
        println("Valid data: ${result.data}")
    }
    is ZodResult.Failure -> {
        result.error.errors.forEach { error ->
            println("Validation error: $error")
        }
    }
}
```