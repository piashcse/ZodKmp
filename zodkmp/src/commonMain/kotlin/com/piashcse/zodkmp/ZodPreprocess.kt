package com.piashcse.zodkmp

/**
 * Schema that applies a preprocessor to the input before validating it with the wrapped schema.
 * Mirrors Zod's `z.preprocess(fn, schema)`.
 */
public class ZodPreprocess<I> internal constructor(
    private val schema: ZodSchema<I>,
    private val preprocessor: (Any?) -> Any?,
) : ZodSchema<I> {
    internal fun wrapped(): ZodSchema<I> = schema

    public companion object {
        public fun <I> schema(
            schema: ZodSchema<I>,
            preprocessor: (Any?) -> Any?,
        ): ZodPreprocess<I> = ZodPreprocess(schema, preprocessor)
    }

    override fun safeParse(input: Any?): ZodResult<I> = schema.safeParse(preprocessor(input))

    override suspend fun safeParseSuspend(input: Any?): ZodResult<I> = schema.safeParseSuspend(preprocessor(input))
}

public fun <I> ZodSchema<I>.preprocess(preprocessor: (Any?) -> Any?): ZodPreprocess<I> = ZodPreprocess(this, preprocessor)
