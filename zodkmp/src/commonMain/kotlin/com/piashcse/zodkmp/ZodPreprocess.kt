package com.piashcse.zodkmp

/**
 * Schema that applies a preprocessor to the input before validating it with the wrapped schema.
 * Mirrors Zod's `z.preprocess(fn, schema)`.
 */
class ZodPreprocess<I> internal constructor(
    private val schema: ZodSchema<I>,
    private val preprocessor: (Any?) -> Any?
) : ZodSchema<I> {
    companion object {
        fun <I> schema(schema: ZodSchema<I>, preprocessor: (Any?) -> Any?): ZodPreprocess<I> =
            ZodPreprocess(schema, preprocessor)
    }
    
    override fun parse(input: Any?): I {
        return schema.parse(preprocessor(input))
    }
    
    override fun safeParse(input: Any?): ZodResult<I> {
        return schema.safeParse(preprocessor(input))
    }
}

fun <I> ZodSchema<I>.preprocess(preprocessor: (Any?) -> Any?): ZodPreprocess<I> =
    ZodPreprocess(this, preprocessor)
