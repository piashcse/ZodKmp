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