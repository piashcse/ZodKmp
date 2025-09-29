package com.piashcse.zodkmp

import kotlin.jvm.JvmInline

/**
 * Schema for validating booleans
 */
@JvmInline
value class ZodBoolean private constructor(private val validations: List<(Boolean) -> ZodError?>) : ZodSchema<Boolean> {
    companion object {
        fun schema(): ZodBoolean = ZodBoolean(emptyList())
    }
    
    fun isEqual(value: Boolean, message: String = "Boolean must be equal to $value"): ZodBoolean {
        val validation: (Boolean) -> ZodError? = { bool ->
            if (bool != value) ZodError(message) else null
        }
        return ZodBoolean(validations + validation)
    }
    
    override fun parse(input: Any?): Boolean {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<Boolean> {
        val booleanInput = when (input) {
            is Boolean -> input
            is String -> {
                when (input.lowercase()) {
                    "true" -> true
                    "false" -> false
                    else -> null
                }
            }
            else -> null
        }
        
        if (booleanInput == null) {
            return ZodResult.Failure(ZodError("Expected boolean, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        val errors = mutableListOf<String>()
        
        for (validation in validations) {
            val error = validation(booleanInput)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        
        return if (errors.isEmpty()) {
            ZodResult.Success(booleanInput)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}