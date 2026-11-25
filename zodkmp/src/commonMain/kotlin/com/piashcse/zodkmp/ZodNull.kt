package com.piashcse.zodkmp

/**
 * Schema for validating null values.
 */
class ZodNull private constructor() : ZodSchema<Any?> {
    companion object {
        fun schema(): ZodNull = ZodNull()
    }

    override fun safeParse(input: Any?): ZodResult<Any?> {
        if (input != null) {
            return ZodResult.Failure(typeError("null", input))
        }
        return ZodResult.Success(null)
    }
}
