package com.piashcse.zodkmp

import kotlin.jvm.JvmInline

/**
 * Schema for validating numbers
 */
@JvmInline
value class ZodNumber private constructor(private val validations: List<(Double) -> ZodError?>) : ZodSchema<Double> {
    companion object {
        fun schema(): ZodNumber = ZodNumber(emptyList())
    }
    
    fun min(value: Double, message: String = "Number must be greater than or equal to $value"): ZodNumber {
        val validation: (Double) -> ZodError? = { num ->
            if (num < value) ZodError(message) else null
        }
        return ZodNumber(validations + validation)
    }
    
    fun max(value: Double, message: String = "Number must be less than or equal to $value"): ZodNumber {
        val validation: (Double) -> ZodError? = { num ->
            if (num > value) ZodError(message) else null
        }
        return ZodNumber(validations + validation)
    }
    
    fun int(message: String = "Number must be an integer"): ZodNumber {
        val validation: (Double) -> ZodError? = { num ->
            if (num % 1 != 0.0) ZodError(message) else null
        }
        return ZodNumber(validations + validation)
    }
    
    fun positive(message: String = "Number must be positive"): ZodNumber {
        val validation: (Double) -> ZodError? = { num ->
            if (num <= 0) ZodError(message) else null
        }
        return ZodNumber(validations + validation)
    }
    
    fun negative(message: String = "Number must be negative"): ZodNumber {
        val validation: (Double) -> ZodError? = { num ->
            if (num >= 0) ZodError(message) else null
        }
        return ZodNumber(validations + validation)
    }
    
    fun nonPositive(message: String = "Number must be non-positive (less than or equal to zero)"): ZodNumber {
        val validation: (Double) -> ZodError? = { num ->
            if (num > 0) ZodError(message) else null
        }
        return ZodNumber(validations + validation)
    }
    
    fun nonNegative(message: String = "Number must be non-negative"): ZodNumber {
        val validation: (Double) -> ZodError? = { num ->
            if (num < 0) ZodError(message) else null
        }
        return ZodNumber(validations + validation)
    }
    
    fun range(min: Double, max: Double, message: String = "Number must be between $min and $max"): ZodNumber {
        val validation: (Double) -> ZodError? = { num ->
            if (num < min || num > max) ZodError(message) else null
        }
        return ZodNumber(validations + validation)
    }
    
    override fun parse(input: Any?): Double {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<Double> {
        val numberInput = when (input) {
            is Number -> input.toDouble()
            is String -> try { input.toDouble() } catch (e: NumberFormatException) { null }
            else -> null
        }
        
        if (numberInput == null) {
            return ZodResult.Failure(ZodError("Expected number, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        val errors = mutableListOf<String>()
        
        for (validation in validations) {
            val error = validation(numberInput)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        
        return if (errors.isEmpty()) {
            ZodResult.Success(numberInput)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}