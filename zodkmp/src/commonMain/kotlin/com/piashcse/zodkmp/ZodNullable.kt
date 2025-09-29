package com.piashcse.zodkmp

import kotlin.jvm.JvmInline

/**
 * Schema for validating nullable values
 */
@JvmInline
value class ZodNullable<T>(
    private val schema: ZodSchema<T>
) : ZodSchema<T?> {
    override fun parse(input: Any?): T? {
        return if (input == null) null else schema.parse(input)
    }

    override fun safeParse(input: Any?): ZodResult<T?> {
        return if (input == null) {
            ZodResult.Success(null)
        } else {
            val result = schema.safeParse(input)
            when (result) {
                is ZodResult.Success -> ZodResult.Success(result.data)
                is ZodResult.Failure -> result
            }
        }
    }
}

fun <T> ZodSchema<T>.nullable(): ZodNullable<T> = ZodNullable(this)