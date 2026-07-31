package com.piashcse.zodkmp

/**
 * Schema for validating objects
 */
data class ZodObjectSchema<T>(
    val shape: Map<String, ZodSchema<*>>,
    private val parser: (Map<String, Any?>) -> T,
    private val strict: Boolean = false,
    private val passthrough: Boolean = false
) : ZodSchema<T> {
    
    fun strict(): ZodObjectSchema<T> = copy(strict = true)
    
    fun strip(): ZodObjectSchema<T> = copy(strict = false, passthrough = false)
    
    fun passthrough(): ZodObjectSchema<T> = copy(passthrough = true)
    
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
        val newShape = shape.mapValues { (_, schema) -> deepPartialSchema(schema) }
        return ZodObjectSchema(newShape, { it }, strict, passthrough)
    }
    
    fun merge(other: ZodObjectSchema<*>): ZodObjectSchema<Map<String, Any?>> {
        val conflicts = shape.keys intersect other.shape.keys
        require(conflicts.isEmpty()) {
            "Invalid merge: key(s) present in both schemas: ${conflicts.joinToString(", ")}"
        }
        val merged = LinkedHashMap(shape)
        merged.putAll(other.shape)
        return ZodObjectSchema(merged, { it }, strict || other.strict, passthrough)
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
        if (mapInput == null) {
            return ZodResult.Failure(ZodError("Expected object, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        val stringKeyMap = mapInput.mapKeys { it.key.toString() }
        
        val errors = mutableListOf<String>()
        val parsedValues = mutableMapOf<String, Any?>()
        
        // Validate all required fields
        for ((key, schema) in shape) {
            val value = stringKeyMap[key]
            val result = schema.safeParse(value)
            
            when (result) {
                is ZodResult.Success -> parsedValues[key] = result.data
                is ZodResult.Failure -> {
                    result.error.errors.forEach { error ->
                        errors.add("$key: $error")
                    }
                }
            }
        }
        
        // Check for extra fields if strict mode is enabled
        if (strict) {
            val extraKeys = stringKeyMap.keys - shape.keys
            if (extraKeys.isNotEmpty()) {
                errors.add("Unrecognized key(s) in object: ${extraKeys.joinToString(", ")}")
            }
        } else if (passthrough) {
            // Keep any extra fields that were not defined in the shape
            stringKeyMap.filterKeys { it !in shape.keys }.forEach { (key, value) ->
                parsedValues[key] = value
            }
        }
        
        return if (errors.isEmpty()) {
            try {
                ZodResult.Success(parser(parsedValues as Map<String, Any?>))
            } catch (e: Exception) {
                ZodResult.Failure(ZodError("Failed to construct object: ${e.message}"))
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
        
        inline fun <reified T> build(
            shapeBuilder: ZodObjectShapeBuilder.() -> Unit,
            noinline parser: (Map<String, Any?>) -> T
        ): ZodObjectSchema<T> {
            val builder = ZodObjectShapeBuilder()
            builder.shapeBuilder()
            return ZodObjectSchema(builder.shape, parser, false, false)
        }
    }
}

private fun deepPartialSchema(schema: ZodSchema<*>): ZodSchema<*> = when (schema) {
    is ZodObjectSchema<*> -> schema.deepPartial().optional()
    is ZodArray<*> -> ZodArray.schema(deepPartialSchema(schema.elementSchema)).optional()
    is ZodSet<*> -> ZodSet.schema(deepPartialSchema(schema.elementSchema)).optional()
    else -> schema.optional()
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

fun <T> ZodObjectShapeBuilder.optionalString(name: String, schema: ZodString = ZodString.schema()) {
    shape[name] = schema.optional()
}

fun <T> ZodObjectShapeBuilder.optionalNumber(name: String, schema: ZodNumber = ZodNumber.schema()) {
    shape[name] = schema.optional()
}

fun <T> ZodObjectShapeBuilder.optionalBoolean(name: String, schema: ZodBoolean = ZodBoolean.schema()) {
    shape[name] = schema.optional()
}

fun <T> ZodObjectShapeBuilder.optionalField(name: String, schema: ZodSchema<T>) {
    shape[name] = schema.optional()
}
