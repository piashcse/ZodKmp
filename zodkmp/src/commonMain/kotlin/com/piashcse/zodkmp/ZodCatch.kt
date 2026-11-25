package com.piashcse.zodkmp

/**
 * Schema that returns a fallback value when the wrapped schema fails to parse.
 * Mirrors Zod's `.catch(fn)`.
 */
class ZodCatch<T> internal constructor(
    private val schema: ZodSchema<T>,
    private val fallback: (Any?) -> T
) : ZodSchema<T> {
    companion object {
        fun <T> schema(schema: ZodSchema<T>, fallback: (Any?) -> T): ZodCatch<T> =
            ZodCatch(schema, fallback)
    }

    override fun parse(input: Any?): T {
        val result = schema.safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> fallback(publicInput(input))
        }
    }

    override fun safeParse(input: Any?): ZodResult<T> {
        val result = schema.safeParse(input)
        return when (result) {
            is ZodResult.Success -> result
            is ZodResult.Failure -> ZodResult.Success(fallback(publicInput(input)))
        }
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T> {
        val result = schema.safeParseSuspend(input)
        return when (result) {
            is ZodResult.Success -> result
            is ZodResult.Failure -> ZodResult.Success(fallback(publicInput(input)))
        }
    }

    private fun publicInput(input: Any?): Any? = if (input === ZodAbsent) null else input
}

fun <T> ZodSchema<T>.catch(fallback: T): ZodCatch<T> = ZodCatch(this, { fallback })

fun <T> ZodSchema<T>.catch(fallback: (Any?) -> T): ZodCatch<T> = ZodCatch(this, fallback)
