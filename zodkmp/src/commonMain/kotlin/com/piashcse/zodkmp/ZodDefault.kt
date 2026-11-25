package com.piashcse.zodkmp

/**
 * Schema that supplies a default value when the input is missing (`ZodAbsent`) or, for
 * backward compatibility, when it is `null` and the wrapped schema rejects `null`.
 *
 * This means `Zod.string().optional().default("x")` keeps an explicit `null` as `null`,
 * while a missing property receives the default.
 */
public class ZodDefault<T> internal constructor(private val schema: ZodSchema<T>, private val defaultValue: () -> T) : ZodSchema<T> {
    internal fun wrapped(): ZodSchema<T> = schema
    public companion object {
        public fun <T> schema(baseSchema: ZodSchema<T>, defaultValue: () -> T): ZodDefault<T> = ZodDefault(baseSchema, defaultValue)
    }

    override fun safeParse(input: Any?): ZodResult<T> {
        if (input === ZodAbsent) return ZodResult.Success(defaultValue())
        val result = schema.safeParse(input)
        if (result is ZodResult.Failure && input == null) {
            return ZodResult.Success(defaultValue())
        }
        return result
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T> {
        if (input === ZodAbsent) return ZodResult.Success(defaultValue())
        val result = schema.safeParseSuspend(input)
        if (result is ZodResult.Failure && input == null) {
            return ZodResult.Success(defaultValue())
        }
        return result
    }
}

public fun <T> ZodSchema<T>.default(defaultValue: () -> T): ZodDefault<T> = ZodDefault(this, defaultValue)

public fun <T> ZodSchema<T>.default(defaultValue: T): ZodDefault<T> = ZodDefault(this, { defaultValue })
