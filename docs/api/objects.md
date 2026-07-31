# Objects

## Basic Object Validation

Validate complex objects:

```kotlin
val userSchema = Zod.objectSchema<User>({
    string("name", Zod.string().min(2))
    string("email", Zod.string().email())
    number("age", Zod.number().min(0).max(120))
    field("isActive", Zod.boolean().default(true))
}) { map ->
    User(
        name = map["name"] as String,
        email = map["email"] as String,
        age = (map["age"] as Number).toDouble(),
        isActive = map["isActive"] as? Boolean ?: true
    )
}
```

## Strict Objects

Strict objects (reject unknown keys):

```kotlin
val strictUserSchema = Zod.objectSchema<User>({
    string("name", Zod.string().min(2))
    string("email", Zod.string().email())
}) { map ->
    User(
        name = map["name"] as String,
        email = map["email"] as String
    )
}.strict()
```

## Nested Object Validation

```kotlin
val addressSchema = Zod.objectSchema<Address>({
    string("street", Zod.string().min(5))
    string("city", Zod.string().min(2))
    string("zipCode", Zod.string().regex(Regex("\\d{5}"))
}) { map ->
    Address(
        street = map["street"] as String,
        city = map["city"] as String,
        zipCode = map["zipCode"] as String
    )
}

val userWithAddressSchema = Zod.objectSchema<UserWithAddress>({
    string("name", Zod.string().min(2))
    field("address", addressSchema)
}) { map ->
    UserWithAddress(
        name = map["name"] as String,
        address = map["address"] as Address
    )
}
```

## Pick

Create a new schema with only the selected keys:

```kotlin
val userSchema = Zod.objectSchema<User>({
    string("name", Zod.string())
    string("email", Zod.string().email())
    number("age", Zod.number())
}) { map -> /* ... */ }

val publicProfile = userSchema.pick("name", "age")
val result = publicProfile.safeParse(mapOf("name" to "John", "age" to 30)) // Success
```

## Omit

Create a new schema without the specified keys:

```kotlin
val credentialsOnly = userSchema.omit("name", "age")
val result = credentialsOnly.safeParse(mapOf("email" to "john@example.com")) // Success
```

## Partial

Make every field optional:

```kotlin
val updateUser = userSchema.partial()
val result = updateUser.safeParse(mapOf("email" to "new@example.com")) // Success
```

## Deep Partial

Make every field optional recursively, including nested objects, arrays and sets:

```kotlin
val settingsSchema = Zod.objectSchema<Settings>({
    field("profile", Zod.objectSchema<Profile>({
        string("displayName", Zod.string())
        string("bio", Zod.string())
    }) { map -> /* ... */ })
    field("tags", Zod.array(Zod.string()))
}) { map -> /* ... */ }

val updateSettings = settingsSchema.deepPartial()
val result = updateSettings.safeParse(mapOf("profile" to mapOf("displayName" to "Alex"))) // Success
```

## Merge

Combine two object schemas into one. Merged keys must not overlap:

```kotlin
val baseSchema = Zod.objectSchema<Base>({
    string("id", Zod.string())
}) { map -> /* ... */ }

val profileSchema = Zod.objectSchema<Profile>({
    string("name", Zod.string())
}) { map -> /* ... */ }

val combined = baseSchema.merge(profileSchema)
val result = combined.safeParse(mapOf("id" to "u1", "name" to "John")) // Success
```

`and` is an infix alias for `merge`:

```kotlin
val combined = baseSchema and profileSchema
```

## Extend

Combine two object schemas, allowing overlapping keys (the right-hand schema wins):

```kotlin
val extended = baseSchema.extend(profileSchema)
```

## Object Modes

Objects validate in `strip` mode by default (unknown keys are dropped). Switch modes:

```kotlin
// Reject unknown keys
val strictSchema = userSchema.strict()
val result1 = strictSchema.safeParse(mapOf("name" to "John", "unknown" to 1)) // Failure

// Keep unknown keys in the output
val passthroughSchema = userSchema.passthrough()
val result2 = passthroughSchema.safeParse(mapOf("name" to "John", "extra" to true)) // Success

// Drop unknown keys (default)
val stripSchema = userSchema.strip()
```