package com.piashcse.zodkmp

/**
 * Schema for optional values (missing properties and `null`).
 */
public class ZodOptional<T>(private val schema: ZodSchema<T>) : ZodSchema<T?> {
    internal fun wrapped(): ZodSchema<T> = schema
    override fun safeParse(input: Any?): ZodResult<T?> = if (isAbsentOrNull(input)) ZodResult.Success(null) else schema.safeParse(input)

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T?> = if (isAbsentOrNull(input)) ZodResult.Success(null) else schema.safeParseSuspend(input)

    private fun isAbsentOrNull(input: Any?): Boolean = input == null || input === ZodAbsent
}

public fun <T> ZodSchema<T>.optional(): ZodOptional<T> = ZodOptional(this)
