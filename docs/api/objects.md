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