package com.piashcse.zodkmp

/**
 * Schema for validating 64-bit integers (the KMP analog of Zod's `bigint`).
 * Accepts [Long], integral [Number] values and numeric strings.
 */
class ZodLong private constructor(
    private val validations: List<(Long) -> ZodError?>
) : ZodSchema<Long> {
    companion object {
        fun schema(): ZodLong = ZodLong(emptyList())
    }
    
    fun min(value: Long, message: String = "Number must be greater than or equal to $value"): ZodLong {
        val validation: (Long) -> ZodError? = { num ->
            if (num < value) ZodError(message) else null
        }
        return ZodLong(validations + validation)
    }
    
    fun max(value: Long, message: String = "Number must be less than or equal to $value"): ZodLong {
        val validation: (Long) -> ZodError? = { num ->
            if (num > value) ZodError(message) else null
        }
        return ZodLong(validations + validation)
    }
    
    fun positive(message: String = "Number must be positive"): ZodLong {
        val validation: (Long) -> ZodError? = { num ->
            if (num <= 0) ZodError(message) else null
        }
        return ZodLong(validations + validation)
    }
    
    fun negative(message: String = "Number must be negative"): ZodLong {
        val validation: (Long) -> ZodError? = { num ->
            if (num >= 0) ZodError(message) else null
        }
        return ZodLong(validations + validation)
    }
    
    override fun parse(input: Any?): Long {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<Long> {
        val longInput = when (input) {
            is Long -> input
            is Number -> {
                // KJS reports several numeric `is`-checks as true for the same JS number,
                // so rely on `is Number` + integrality instead of per-type checks.
                val value = input.toDouble()
                if (value.isFinite() &&
                    value % 1.0 == 0.0 &&
                    value >= Long.MIN_VALUE.toDouble() &&
                    value <= Long.MAX_VALUE.toDouble()
                ) {
                    value.toLong()
                } else {
                    null
                }
            }
            is String -> input.toLongOrNull()
            else -> null
        }
        
        if (longInput == null) {
            return ZodResult.Failure(ZodError("Expected long, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        val errors = mutableListOf<String>()
        for (validation in validations) {
            val error = validation(longInput)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        
        return if (errors.isEmpty()) ZodResult.Success(longInput) else ZodResult.Failure(ZodError(errors))
    }
}
