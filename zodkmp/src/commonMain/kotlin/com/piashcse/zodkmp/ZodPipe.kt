package com.piashcse.zodkmp

/**
 * Schema that pipes the output of one schema into the input of another.
 * Mirrors Zod's `z.pipe(a, b)`.
 */
class ZodPipe<T, R> internal constructor(
    private val first: ZodSchema<T>,
    private val second: ZodSchema<R>
) : ZodSchema<R> {
    companion object {
        fun <T, R> schema(first: ZodSchema<T>, second: ZodSchema<R>): ZodPipe<T, R> =
            ZodPipe(first, second)
    }
    
    override fun parse(input: Any?): R {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<R> {
        val firstResult = first.safeParse(input)
        if (firstResult is ZodResult.Failure) {
            return firstResult as ZodResult<R>
        }
        @Suppress("UNCHECKED_CAST")
        return second.safeParse((firstResult as ZodResult.Success<T>).data)
    }
}

fun <T, R> ZodSchema<T>.pipe(second: ZodSchema<R>): ZodPipe<T, R> = ZodPipe(this, second)
