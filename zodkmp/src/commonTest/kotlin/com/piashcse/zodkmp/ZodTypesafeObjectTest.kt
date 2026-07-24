package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodTypesafeObjectTest {
    data class User(val name: String, val age: Double)
    data class Product(val id: Int, val name: String, val price: Double)

    @Test
    fun `parse should succeed with valid object`() {
        val builder = ZodTypesafeObjectShapeBuilder<User>()
        with(builder){
            User::name to Zod.string()
            User::age to Zod.number()
        }
        val userSchema = ZodTypesafeObjectSchema.create<User>(
            shape = builder.shape,
            parser = { map -> User(map[User::name], map[User::age]) }
        )
        
        val validData = mapOf("name" to "John", "age" to 30.0)
        val result = userSchema.parse(validData)
        assertEquals(User("John", 30.0), result)
    }

    @Test
    fun `parse should throw with invalid object`() {
        val builder = ZodTypesafeObjectShapeBuilder<User>()
        with(builder){
            User::name to Zod.string()
            User::age to Zod.number()
        }
        val userSchema = ZodTypesafeObjectSchema.create(
            shape = builder.shape,
            parser = { map -> User(map[User::name], map[User::age]) }
        )
        
        val invalidData = mapOf("name" to "John", "age" to "not a number")
        assertFailsWith<IllegalArgumentException> {
            userSchema.parse(invalidData)
        }
    }

    @Test
    fun `safeParse should return Success with valid object`() {
        val builder = ZodTypesafeObjectShapeBuilder<User>()
        with(builder){
            User::name to Zod.string()
            User::age to Zod.number()
        }
        val userSchema = ZodTypesafeObjectSchema.create(
            shape = builder.shape,
            parser = { map -> User(map[User::name], (map[User::age])) }
        )
        
        val validData = mapOf("name" to "John", "age" to 30.0)
        val result = userSchema.safeParse(validData)
        assertTrue(result is ZodResult.Success<*>)
        @Suppress("UNCHECKED_CAST")
        val userData = result.data as User
        assertEquals(User("John", 30.0), userData)
    }

    @Test
    fun `safeParse should return Failure with invalid object`() {
        val builder = ZodTypesafeObjectShapeBuilder<User>()
        with(builder){
            User::name to Zod.string()
            User::age to Zod.number()
        }
        val userSchema = ZodTypesafeObjectSchema.create<User>(
            shape = builder.shape,
            parser = { map -> User(map[User::name], map[User::age]) }
        )
        
        val invalidData = mapOf("name" to "John", "age" to "not a number")
        val result = userSchema.safeParse(invalidData)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("age") })
    }

    @Test
    fun `safeParse should return Failure with non-object input`() {
        val builder = ZodTypesafeObjectShapeBuilder<User>()
        with(builder){
            User::name to Zod.string()
            User::age to Zod.number()
        }
        val userSchema = ZodTypesafeObjectSchema.create<User>(
            shape = builder.shape,
            parser = { map -> User(map[User::name], map[User::age]) }
        )
        
        val result = userSchema.safeParse("not an object")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected object"))
    }

    @Test
    fun `strict mode should reject extra fields`() {
        val builder = ZodTypesafeObjectShapeBuilder<User>()
        with(builder){
            User::name to Zod.string()
            User::age to Zod.number()
        }
        val userSchema = ZodTypesafeObjectSchema.create(
            shape = builder.shape,
            parser = { map -> User(map[User::name], map[User::age]) },
            strict = true
        )
        
        val dataWithExtra = mapOf("name" to "John", "age" to 30.0, "extra" to "field")
        val result = userSchema.safeParse(dataWithExtra)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("Unrecognized key") })
    }

    @Test
    fun `missing required field should fail`() {
        val builder = ZodTypesafeObjectShapeBuilder<User>()
        with(builder){
            User::name to Zod.string()
            User::age to Zod.number()
        }
        val userSchema = ZodTypesafeObjectSchema.create<User>(
            shape = builder.shape,
            parser = { map -> User(map[User::name], map[User::age]) }
        )
        
        val dataWithMissing = mapOf("name" to "John") // Missing age
        val result = userSchema.safeParse(dataWithMissing)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("age") })
    }

    @Test
    fun `optional fields using optional extensions should work`() {
        data class Person(val name: String, val age: Double?)

        val builder = ZodTypesafeObjectShapeBuilder<Person>()
        with(builder){
            Person::name to Zod.string()
            Person::age to Zod.number().nullable()
        }
        val schema = ZodTypesafeObjectSchema.create<Person>(
            shape = builder.shape,
            parser = { map -> 
                val ageValue = map[Person::age]
                Person(map[Person::name], ageValue)
            }
        )
        
        // With optional field present
        val dataWithAge = mapOf("name" to "John", "age" to 30.0)
        val result1 = schema.safeParse(dataWithAge)
        assertTrue(result1 is ZodResult.Success<*>)
        @Suppress("UNCHECKED_CAST")
        val person1 = result1.data as Person
        assertEquals(Person("John", 30.0), person1)
        
        // With optional field absent (using null)
        val dataWithoutAge = mapOf("name" to "John", "age" to null)
        val result2 = schema.safeParse(dataWithoutAge)
        assertTrue(result2 is ZodResult.Success<*>)
        @Suppress("UNCHECKED_CAST")
        val person2 = result2.data as Person
        assertEquals(Person("John", null), person2)
    }
}
