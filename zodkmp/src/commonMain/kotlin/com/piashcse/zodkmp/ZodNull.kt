package com.piashcse.zodkmp

/**
 * Schema for validating null values.
 */
public class ZodNull private constructor() : ZodSchema<Any?> {
    public companion object {
        public fun schema(): ZodNull = ZodNull()
    }

    override fun safeParse(input: Any?): ZodResult<Any?> {
        if (input != null) {
            return ZodResult.Failure(typeError("null", input))
        }
        return ZodResult.Success(null)
    }
}
