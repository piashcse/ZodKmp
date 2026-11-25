package com.piashcse.zodkmp

/**
 * Schema for nullable values: accepts `null` in addition to the wrapped schema.
 * Unlike [ZodOptional], a missing object property is not accepted.
 */
public class ZodNullable<T>(private val schema: ZodSchema<T>) : ZodSchema<T?> {
    internal fun wrapped(): ZodSchema<T> = schema
    override fun safeParse(input: Any?): ZodResult<T?> = if (input == null) ZodResult.Success(null) else schema.safeParse(input)

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T?> = if (input == null) ZodResult.Success(null) else schema.safeParseSuspend(input)
}

public fun <T> ZodSchema<T>.nullable(): ZodNullable<T> = ZodNullable(this)
