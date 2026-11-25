package com.piashcse.zodkmp

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
    
    override fun safeParse(input: Any?): ZodResult<O> {
        val result = schema.safeParse(input)
        return when (result) {
            is ZodResult.Success -> applyTransform(result.data)
            is ZodResult.Failure -> result as ZodResult<O> // Safe cast due to variance
        }
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<O> {
        val result = schema.safeParseSuspend(input)
        return when (result) {
            is ZodResult.Success -> applyTransform(result.data)
            is ZodResult.Failure -> result as ZodResult<O>
        }
    }

    private fun applyTransform(value: I): ZodResult<O> {
        return try {
            ZodResult.Success(transform(value))
        } catch (e: Exception) {
            ZodResult.Failure(
                ZodError("Transformation failed: ${e::class.simpleName}: ${e.message}")
            )
        }
    }
}

fun <I, O> ZodSchema<I>.transform(transform: (I) -> O): ZodTransform<I, O> = ZodTransform(this, transform)
