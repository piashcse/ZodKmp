# Unions

## Basic Unions

Validate values that match any of multiple schemas:

```kotlin
val stringOrNumber = Zod.union(Zod.string(), Zod.number())
val complexUnion = Zod.union(
    Zod.string(),
    Zod.number(),
    Zod.objectSchema<Point>({
        number("x", Zod.number())
        number("y", Zod.number())
    }) { map ->
        Point(
            x = (map["x"] as Number).toDouble(),
            y = (map["y"] as Number).toDouble()
        )
    }
)
```

## Discriminated Unions

`discriminatedUnion` maps objects into a `sealed class` hierarchy. The literal
value of a discriminator key selects which object schema is applied:

```kotlin
sealed class Animal {
    data class Dog(val name: String, val goodBoy: Boolean) : Animal()
    data class Cat(val name: String, val lives: Double) : Animal()
}

val animalSchema = Zod.discriminatedUnion<Animal>(
    discriminator = "type",
    Zod.objectSchema<Animal.Dog>({
        field("type", Zod.literal("dog"))
        string("name", Zod.string())
        boolean("goodBoy", Zod.boolean())
    }) { map ->
        Animal.Dog(map["name"] as String, map["goodBoy"] as Boolean)
    },
    Zod.objectSchema<Animal.Cat>({
        field("type", Zod.literal("cat"))
        string("name", Zod.string())
        number("lives", Zod.number())
    }) { map ->
        Animal.Cat(map["name"] as String, map["lives"] as Double)
    }
)

val result = animalSchema.safeParse(mapOf("type" to "dog", "name" to "Rex", "goodBoy" to true))
// Success with Animal.Dog
```

`discriminatedUnion` is also available with a list of options:

```kotlin
val animalSchema = Zod.discriminatedUnion<Animal>("type", listOf(dogSchema, catSchema))
```

The discriminator key should be declared with `Zod.literal(...)` on each option.
If no literal matches the input value, the schema falls back to trying each option
in order and returns the first that parses successfully.