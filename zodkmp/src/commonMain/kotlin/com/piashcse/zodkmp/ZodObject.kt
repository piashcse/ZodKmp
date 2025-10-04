package com.piashcse.zodkmp

/**
 * Schema for validating objects
 */
data class ZodObjectSchema<T>(
    private val shape: Map<String, ZodSchema<*>>,
    private val parser: (Map<String, Any?>) -> T,
    private val strict: Boolean = false
) : ZodSchema<T> {
    
    fun strict(): ZodObjectSchema<T> = copy(strict = true)
    
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
            return ZodObjectSchema(shape, parser, strict)
        }
        
        inline fun <reified T> build(
            shapeBuilder: ZodObjectShapeBuilder.() -> Unit,
            noinline parser: (Map<String, Any?>) -> T
        ): ZodObjectSchema<T> {
            val builder = ZodObjectShapeBuilder()
            builder.shapeBuilder()
            return ZodObjectSchema(builder.shape, parser, false)
        }
    }
}

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