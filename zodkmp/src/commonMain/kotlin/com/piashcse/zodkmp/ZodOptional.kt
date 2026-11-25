package com.piashcse.zodkmp

/**
 * Schema for optional values (missing properties and `null`).
 */
class ZodOptional<T>(
    private val schema: ZodSchema<T>
) : ZodSchema<T?> {
    override fun parse(input: Any?): T? = if (isAbsentOrNull(input)) null else schema.parse(input)

    override fun safeParse(input: Any?): ZodResult<T?> =
        if (isAbsentOrNull(input)) ZodResult.Success(null) else schema.safeParse(input)

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T?> =
        if (isAbsentOrNull(input)) ZodResult.Success(null) else schema.safeParseSuspend(input)

    private fun isAbsentOrNull(input: Any?): Boolean = input == null || input === ZodAbsent
}

fun <T> ZodSchema<T>.optional(): ZodOptional<T> = ZodOptional(this)
