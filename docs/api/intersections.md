# Intersections

## Intersections

Validate values that must satisfy multiple schemas:

```kotlin
val personSchema = Zod.objectSchema<Person>({
    string("name", Zod.string())
}) { map ->
    Person(name = map["name"] as String)
}

val employeeSchema = Zod.objectSchema<Employee>({
    string("employeeId", Zod.string())
}) { map ->
    Employee(employeeId = map["employeeId"] as String)
}

val personEmployeeSchema = Zod.intersection(personSchema, employeeSchema)
```