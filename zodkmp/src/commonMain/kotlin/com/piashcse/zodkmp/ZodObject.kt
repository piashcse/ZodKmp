package com.piashcse.zodkmp

/**
 * Schema for validating objects.
 */
data class ZodObjectSchema<T>(
    val shape: Map<String, ZodSchema<*>>,
    private val parser: (Map<String, Any?>) -> T,
    private val strict: Boolean = false,
    private val passthrough: Boolean = false
) : ZodSchema<T> {

    fun strict(): ZodObjectSchema<T> = copy(strict = true, passthrough = false)

    fun strip(): ZodObjectSchema<T> = copy(strict = false, passthrough = false)

    fun passthrough(): ZodObjectSchema<T> = copy(strict = false, passthrough = true)

    fun pick(vararg keys: String): ZodObjectSchema<Map<String, Any?>> {
        val selected = shape.filterKeys { it in keys }
        return ZodObjectSchema(selected, { it }, strict, passthrough)
    }

    fun omit(vararg keys: String): ZodObjectSchema<Map<String, Any?>> {
        val remaining = shape.filterKeys { it !in keys }
        return ZodObjectSchema(remaining, { it }, strict, passthrough)
    }

    fun partial(): ZodObjectSchema<Map<String, Any?>> {
        val newShape = shape.mapValues { (_, schema) -> schema.optional() as ZodSchema<*> }
        return ZodObjectSchema(newShape, { it }, strict, passthrough)
    }

    fun deepPartial(): ZodObjectSchema<Map<String, Any?>> {
        val newShape = shape.mapValues { (_, schema) -> deepPartialProperty(schema) }
        return ZodObjectSchema(newShape, { it }, strict, passthrough)
    }

    /**
     * Merges [other] into this schema. Later definitions win for duplicate keys,
     * matching Zod's `merge`/`extend` behaviour.
     */
    fun merge(other: ZodObjectSchema<*>): ZodObjectSchema<Map<String, Any?>> {
        val merged = LinkedHashMap(shape)
        merged.putAll(other.shape)
        val mergedStrict = strict || other.strict
        val mergedPassthrough = !mergedStrict && (passthrough || other.passthrough)
        return ZodObjectSchema(merged, { it }, mergedStrict, mergedPassthrough)
    }

    fun extend(other: ZodObjectSchema<*>): ZodObjectSchema<Map<String, Any?>> {
        val merged = LinkedHashMap(shape)
        merged.putAll(other.shape)
        return ZodObjectSchema(merged, { it }, strict, passthrough)
    }

    override fun parse(input: Any?): T {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }

    override fun safeParse(input: Any?): ZodResult<T> {
        val mapInput = input as? Map<*, *>
            ?: return ZodResult.Failure(ZodError("Expected object, received ${input.typeName()}"))

        val nonStringKey = mapInput.keys.firstOrNull { it !is String }
        if (nonStringKey != null) {
            return ZodResult.Failure(
                ZodError("Expected object with string keys, received key of type ${nonStringKey.typeName()}")
            )
        }

        @Suppress("UNCHECKED_CAST")
        val stringKeyMap = mapInput as Map<String, Any?>

        val errors = mutableListOf<String>()
        val parsedValues = mutableMapOf<String, Any?>()

        for ((key, schema) in shape) {
            val present = stringKeyMap.containsKey(key)
            val value: Any? = if (present) stringKeyMap[key] else ZodAbsent
            val result = schema.safeParse(value)

            when (result) {
                is ZodResult.Success -> parsedValues[key] = result.data
                is ZodResult.Failure -> {
                    val messages = if (present) result.error.errors else listOf("Required")
                    messages.forEach { error -> errors.add("$key: $error") }
                }
            }
        }

        if (strict) {
            val extraKeys = stringKeyMap.keys - shape.keys
            if (extraKeys.isNotEmpty()) {
                errors.add("Unrecognized key(s) in object: ${extraKeys.joinToString(", ")}")
            }
        } else if (passthrough) {
            stringKeyMap.filterKeys { it !in shape.keys }.forEach { (key, value) ->
                parsedValues[key] = value
            }
        }

        return if (errors.isEmpty()) {
            try {
                ZodResult.Success(parser(parsedValues as Map<String, Any?>))
            } catch (e: Exception) {
                ZodResult.Failure(
                    ZodError("Failed to construct object: ${e::class.simpleName}: ${e.message}")
                )
            }
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T> {
        val mapInput = input as? Map<*, *>
            ?: return ZodResult.Failure(ZodError("Expected object, received ${input.typeName()}"))

        val nonStringKey = mapInput.keys.firstOrNull { it !is String }
        if (nonStringKey != null) {
            return ZodResult.Failure(
                ZodError("Expected object with string keys, received key of type ${nonStringKey.typeName()}")
            )
        }

        @Suppress("UNCHECKED_CAST")
        val stringKeyMap = mapInput as Map<String, Any?>

        val errors = mutableListOf<String>()
        val parsedValues = mutableMapOf<String, Any?>()

        for ((key, schema) in shape) {
            val present = stringKeyMap.containsKey(key)
            val value: Any? = if (present) stringKeyMap[key] else ZodAbsent
            when (val result = schema.safeParseSuspend(value)) {
                is ZodResult.Success -> parsedValues[key] = result.data
                is ZodResult.Failure -> {
                    val messages = if (present) result.error.errors else listOf("Required")
                    messages.forEach { error -> errors.add("$key: $error") }
                }
            }
        }

        if (strict) {
            val extraKeys = stringKeyMap.keys - shape.keys
            if (extraKeys.isNotEmpty()) {
                errors.add("Unrecognized key(s) in object: ${extraKeys.joinToString(", ")}")
            }
        } else if (passthrough) {
            stringKeyMap.filterKeys { it !in shape.keys }.forEach { (key, value) ->
                parsedValues[key] = value
            }
        }

        return if (errors.isEmpty()) {
            try {
                ZodResult.Success(parser(parsedValues as Map<String, Any?>))
            } catch (e: Exception) {
                ZodResult.Failure(
                    ZodError("Failed to construct object: ${e::class.simpleName}: ${e.message}")
                )
            }
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }

    companion object {
        fun <T> create(
            shape: Map<String, ZodSchema<*>>,
            parser: (Map<String, Any?>) -> T,
            strict: Boolean = false
        ): ZodObjectSchema<T> {
            return ZodObjectSchema(shape, parser, strict, false)
        }

        fun <T> create(
            shape: Map<String, ZodSchema<*>>,
            parser: (Map<String, Any?>) -> T,
            strict: Boolean,
            passthrough: Boolean
        ): ZodObjectSchema<T> {
            return ZodObjectSchema(shape, parser, strict, passthrough)
        }

        fun <T> build(
            shapeBuilder: ZodObjectShapeBuilder.() -> Unit,
            parser: (Map<String, Any?>) -> T
        ): ZodObjectSchema<T> {
            val builder = ZodObjectShapeBuilder()
            builder.shapeBuilder()
            return ZodObjectSchema(builder.shape, parser, false, false)
        }
    }
}

/**
 * Makes a property optional; nested object properties are made optional too, while
 * collection constraints (`min`, `max`, `unique`, ...) are preserved.
 */
private fun deepPartialProperty(schema: ZodSchema<*>): ZodSchema<*> = when (schema) {
    is ZodObjectSchema<*> -> schema.deepPartial().optional()
    is ZodArray<*> -> schema.withElement(deepPartialElement(schema.elementSchema)).optional()
    is ZodSet<*> -> schema.withElement(deepPartialElement(schema.elementSchema)).optional()
    else -> schema.optional()
}

private fun deepPartialElement(schema: ZodSchema<*>): ZodSchema<*> = when (schema) {
    is ZodObjectSchema<*> -> schema.deepPartial()
    is ZodArray<*> -> schema.withElement(deepPartialElement(schema.elementSchema))
    is ZodSet<*> -> schema.withElement(deepPartialElement(schema.elementSchema))
    else -> schema
}

infix fun <T, U> ZodObjectSchema<T>.and(other: ZodObjectSchema<U>): ZodObjectSchema<Map<String, Any?>> = merge(other)

class ZodObjectShapeBuilder {
    val shape: MutableMap<String, ZodSchema<*>> = mutableMapOf()

    fun string(name: String, schema: ZodString = ZodString.schema()) {
        shape[name] = schema
    }

    fun number(name: String, schema: ZodNumber = ZodNumber.schema()) {
        shape[name] = schema
    }

    fun boolean(name: String, schema: ZodBoolean = ZodBoolean.schema()) {
        shape[name] = schema
    }

    fun <T> field(name: String, schema: ZodSchema<T>) {
        shape[name] = schema
    }
}

fun ZodObjectShapeBuilder.optionalString(name: String, schema: ZodString = ZodString.schema()) {
    shape[name] = schema.optional()
}

fun ZodObjectShapeBuilder.optionalNumber(name: String, schema: ZodNumber = ZodNumber.schema()) {
    shape[name] = schema.optional()
}

fun ZodObjectShapeBuilder.optionalBoolean(name: String, schema: ZodBoolean = ZodBoolean.schema()) {
    shape[name] = schema.optional()
}

fun <T> ZodObjectShapeBuilder.optionalField(name: String, schema: ZodSchema<T>) {
    shape[name] = schema.optional()
}
