package com.piashcse.zodkmp

/**
 * Schema wrapper that carries a description / metadata without changing validation behaviour.
 * Mirrors Zod's `.describe()` and `.meta()`.
 */
class ZodDescribe<T> internal constructor(
    val schema: ZodSchema<T>,
    val description: String,
    val metadata: Map<String, Any?>
) : ZodSchema<T> {
    override fun parse(input: Any?): T = schema.parse(input)
    
    override fun safeParse(input: Any?): ZodResult<T> = schema.safeParse(input)
}

fun <T> ZodSchema<T>.describe(description: String): ZodDescribe<T> =
    ZodDescribe(this, description, emptyMap())

fun <T> ZodSchema<T>.meta(metadata: Map<String, Any?>): ZodDescribe<T> =
    ZodDescribe(this, "", metadata)
