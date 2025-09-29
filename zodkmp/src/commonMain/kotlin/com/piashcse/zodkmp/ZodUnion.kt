package com.piashcse.zodkmp

import kotlin.jvm.JvmInline

/**
 * Schema for validating union types (accepts values that match any of the provided schemas)
 */
class ZodUnion private constructor(
    private val schemas: List<ZodSchema<*>>,
    private val validations: List<(Any?) -> ZodError?>
) : ZodSchema<Any?> {
    companion object {
        fun schema(vararg schemas: ZodSchema<*>): ZodUnion = ZodUnion(schemas.toList(), emptyList())
        fun schema(schemas: List<ZodSchema<*>>): ZodUnion = ZodUnion(schemas, emptyList())
    }
    
    override fun parse(input: Any?): Any? {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<Any?> {
        val errors = mutableListOf<String>()
        var lastError: ZodError? = null
        
        for ((index, schema) in schemas.withIndex()) {
            val result = schema.safeParse(input)
            when (result) {
                is ZodResult.Success -> {
                    // Apply union-level validations
                    for (validation in validations) {
                        val error = validation(result.data)
                        if (error != null) {
                            return ZodResult.Failure(error)
                        }
                    }
                    return ZodResult.Success(result.data)
                }
                is ZodResult.Failure -> {
                    lastError = result.error
                    errors.add("Option $index: ${result.error.errors.joinToString(", ")}")
                }
            }
        }
        
        return ZodResult.Failure(ZodError("Union validation failed: ${errors.joinToString("; ")}"))
    }
}