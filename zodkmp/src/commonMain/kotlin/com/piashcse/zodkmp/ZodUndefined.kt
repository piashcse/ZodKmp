package com.piashcse.zodkmp

import kotlin.jvm.JvmInline

/**
 * Schema for validating undefined values (represented as null in Kotlin)
 */
class ZodUndefined internal constructor(
    private val validations: List<(Any?) -> ZodError?>
) : ZodSchema<Any?> {
    companion object {
        fun schema(): ZodUndefined = ZodUndefined(emptyList())
    }
    
    override fun parse(input: Any?): Any? {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<Any?> {
        // In Kotlin/JS, undefined is typically represented differently, but for multiplatform compatibility
        // we can consider null as undefined
        if (input != null) {
            return ZodResult.Failure(ZodError("Expected undefined, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        val errors = mutableListOf<String>()
        
        for (validation in validations) {
            val error = validation(input)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        
        return if (errors.isEmpty()) {
            ZodResult.Success(input)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}