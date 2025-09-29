package com.piashcse.zodkmp

import kotlin.jvm.JvmInline

/**
 * Schema for transforming values
 */
class ZodTransform<I, O> internal constructor(
    private val schema: ZodSchema<I>,
    private val transform: (I) -> O
) : ZodSchema<O> {
    companion object {
        fun <I, O> schema(baseSchema: ZodSchema<I>, transform: (I) -> O): ZodTransform<I, O> 
            = ZodTransform(baseSchema, transform)
    }
    
    override fun parse(input: Any?): O {
        val parsedValue = schema.parse(input)
        return transform(parsedValue)
    }
    
    override fun safeParse(input: Any?): ZodResult<O> {
        val result = schema.safeParse(input)
        return when (result) {
            is ZodResult.Success -> {
                try {
                    val transformedValue = transform(result.data)
                    ZodResult.Success(transformedValue)
                } catch (e: Exception) {
                    ZodResult.Failure(ZodError("Transformation failed: ${e.message}"))
                }
            }
            is ZodResult.Failure -> result as ZodResult<O> // Safe cast due to variance
        }
    }
}

fun <I, O> ZodSchema<I>.transform(transform: (I) -> O): ZodTransform<I, O> = ZodTransform(this, transform)