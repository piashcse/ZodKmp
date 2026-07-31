package com.piashcse.zodkmp

import kotlin.reflect.KProperty1

/**
 * Schema for validating objects
 */
data class ZodTypesafeObjectSchema<T>(
    private val shape: Map<PropertyContainer<T, *>, ZodSchema<*>>,
    private val parser: (TypeSafeParsed<T>) -> T,
    private val strict: Boolean = false
) : ZodSchema<T> {

    fun strict(): ZodTypesafeObjectSchema<T> = copy(strict = true)

    override fun parse(input: Any?): T {
        return when (val result = safeParse(input)) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException(
                "Validation failed: ${
                    result.error.errors.joinToString(
                        ", "
                    )
                }"
            )
        }
    }

    override fun safeParse(input: Any?): ZodResult<T> {
        val mapInput = input as? Map<*, *>
        if (mapInput == null) {
            return ZodResult.Failure(ZodError("Expected object, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }

        val stringKeys = mapInput.mapKeys { it.key.toString() }

        val errors = mutableListOf<String>()

        val parsedValues = mutableMapOf<PropertyContainer<T, *>, Any?>()

        // Validate all required fields
        for ((key, schema) in shape) {
            val value = stringKeys[key.name]
            when (val result = schema.safeParse(value)) {
                is ZodResult.Success -> parsedValues[key] = result.data
                is ZodResult.Failure -> {
                    result.error.errors.forEach { error ->
                        errors.add("${key.name}: $error")
                    }
                }
            }
        }

        // Check for extra fields if strict mode is enabled
        if (strict) {
            val extraKeys = stringKeys.keys - shape.keys
            if (extraKeys.isNotEmpty()) {
                errors.add("Unrecognized key(s) in object: ${extraKeys.joinToString(", ")}")
            }
        }

        return if (errors.isEmpty()) {
            try {
                ZodResult.Success(parser(TypeSafeParsed(parsedValues)))
            } catch (e: Exception) {
                ZodResult.Failure(ZodError("Failed to construct object: ${e.message}"))
            }
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }

    companion object {
        fun <T> create(
            shape: Map<PropertyContainer<T, *>, ZodSchema<*>>,
            parser: (TypeSafeParsed<T>) -> T,
            strict: Boolean = false
        ): ZodTypesafeObjectSchema<T> {
            return ZodTypesafeObjectSchema(shape, parser, strict)
        }

        inline fun <reified T> build(
            shapeBuilder: ZodTypesafeObjectShapeBuilder<T>.() -> Unit,
            noinline parser: (TypeSafeParsed<T>) -> T
        ): ZodTypesafeObjectSchema<T> {
            val builder = ZodTypesafeObjectShapeBuilder<T>()
            builder.shapeBuilder()
            return ZodTypesafeObjectSchema(builder.shape, parser, false)
        }
    }
}

data class PropertyContainer<T, R>(
    val property: KProperty1<T, R>,
    val name: String,
)

class ZodTypesafeObjectShapeBuilder<T> {
    val shape: MutableMap<PropertyContainer<T, *>, ZodSchema<*>> = mutableMapOf()

    infix fun <R>KProperty1<T, R>.to(schema: ZodSchema<R>) {
        shape[named(name)] = schema
    }
    infix fun <R> PropertyContainer<T, R>.to(schema: ZodSchema<R>) {
        shape[this] = schema
    }
    fun <R>KProperty1<T, R>.named(name: String) = PropertyContainer(this, name)
}

inline fun <reified T> typeSafeObjectSchema(
    shapeBuilder: ZodTypesafeObjectShapeBuilder<T>.()->Unit,
    noinline parser: (TypeSafeParsed<T>) -> T
): ZodTypesafeObjectSchema<T> {
    return ZodTypesafeObjectSchema.build(
        shapeBuilder = shapeBuilder,
        parser = parser
    )
}

class TypeSafeParsed<T> internal constructor(private val from: Map<PropertyContainer<T, *>, Any?>) {

    private val mapped = from.keys.associateBy { it.property }

    operator fun <R> get(kProperty1: KProperty1<T, R>): R {
        @Suppress("UNCHECKED_CAST")
        return from[mapped[kProperty1]] as R
    }
}
