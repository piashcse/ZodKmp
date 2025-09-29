package com.piashcse.zodkmp

import kotlin.jvm.JvmInline

/**
 * Schema for validating intersection types (value must satisfy both schemas)
 */
class ZodIntersection<T, U> private constructor(
    private val leftSchema: ZodSchema<T>,
    private val rightSchema: ZodSchema<U>,
    private val validations: List<(Pair<T, U>) -> ZodError?>
) : ZodSchema<Pair<T, U>> {
    companion object {
        fun <T, U> schema(leftSchema: ZodSchema<T>, rightSchema: ZodSchema<U>): ZodIntersection<T, U> 
            = ZodIntersection(leftSchema, rightSchema, emptyList())
    }
    
    override fun parse(input: Any?): Pair<T, U> {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<Pair<T, U>> {
        // Parse with left schema
        val leftResult = leftSchema.safeParse(input)
        if (leftResult is ZodResult.Failure) {
            return ZodResult.Failure(ZodError("Left schema validation failed: ${leftResult.error.errors.joinToString(", ")}"))
        }
        
        // Parse with right schema
        val rightResult = rightSchema.safeParse(input)
        if (rightResult is ZodResult.Failure) {
            return ZodResult.Failure(ZodError("Right schema validation failed: ${rightResult.error.errors.joinToString(", ")}"))
        }
        
        @Suppress("UNCHECKED_CAST")
        val pairResult = Pair((leftResult as ZodResult.Success<T>).data, (rightResult as ZodResult.Success<U>).data)
        
        // Apply intersection-level validations
        for (validation in validations) {
            val error = validation(pairResult)
            if (error != null) {
                return ZodResult.Failure(error)
            }
        }
        
        return ZodResult.Success(pairResult)
    }
}