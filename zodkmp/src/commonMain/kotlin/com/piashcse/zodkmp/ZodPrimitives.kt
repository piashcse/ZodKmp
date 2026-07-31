package com.piashcse.zodkmp

/**
 * Schema that accepts any value and returns it unchanged.
 */
class ZodAny private constructor(
    private val validations: List<(Any?) -> ZodError?>
) : ZodSchema<Any?> {
    companion object {
        fun schema(): ZodAny = ZodAny(emptyList())
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
        for (validation in validations) {
            val error = validation(input)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        return if (errors.isEmpty()) ZodResult.Success(input) else ZodResult.Failure(ZodError(errors))
    }
}

/**
 * Schema that accepts any value without type information (alias of [ZodAny] in terms of behaviour).
 */
class ZodUnknown private constructor(
    private val validations: List<(Any?) -> ZodError?>
) : ZodSchema<Any?> {
    companion object {
        fun schema(): ZodUnknown = ZodUnknown(emptyList())
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
        for (validation in validations) {
            val error = validation(input)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        return if (errors.isEmpty()) ZodResult.Success(input) else ZodResult.Failure(ZodError(errors))
    }
}

/**
 * Schema that rejects every value.
 */
class ZodNever private constructor() : ZodSchema<Nothing> {
    companion object {
        fun schema(): ZodNever = ZodNever()
    }
    
    override fun parse(input: Any?): Nothing {
        throw IllegalArgumentException("Expected never, received ${input?.let { it::class.simpleName } ?: "null"}")
    }
    
    override fun safeParse(input: Any?): ZodResult<Nothing> {
        return ZodResult.Failure(ZodError("Expected never, received ${input?.let { it::class.simpleName } ?: "null"}"))
    }
}

/**
 * Schema that only accepts null/undefined input and returns [Unit], mirroring Zod's `void`.
 */
class ZodVoid private constructor(
    private val validations: List<(Any?) -> ZodError?>
) : ZodSchema<Unit> {
    companion object {
        fun schema(): ZodVoid = ZodVoid(emptyList())
    }
    
    override fun parse(input: Any?): Unit {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<Unit> {
        if (input != null) {
            return ZodResult.Failure(ZodError("Expected void, received ${input.let { it::class.simpleName } ?: "null"}"))
        }
        val errors = mutableListOf<String>()
        for (validation in validations) {
            val error = validation(input)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        return if (errors.isEmpty()) ZodResult.Success(Unit) else ZodResult.Failure(ZodError(errors))
    }
}
