package com.piashcse.zodkmp

/**
 * Schema wrapper that carries a description / metadata without changing validation behaviour.
 * Mirrors Zod's `.describe()` and `.meta()`.
 */
public class ZodDescribe<T> internal constructor(public val schema: ZodSchema<T>, public val description: String, public val metadata: Map<String, Any?>) :
    ZodSchema<T> {
    override fun safeParse(input: Any?): ZodResult<T> = schema.safeParse(input)

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T> = schema.safeParseSuspend(input)
}

public fun <T> ZodSchema<T>.describe(description: String): ZodDescribe<T> = ZodDescribe(this, description, emptyMap())

public fun <T> ZodSchema<T>.meta(metadata: Map<String, Any?>): ZodDescribe<T> = ZodDescribe(this, "", metadata)
