package com.piashcse.zodkmp

/**
 * Schema for validating undefined values (represented as `null` or a missing property in Kotlin).
 */
public class ZodUndefined private constructor() : ZodSchema<Any?> {
    public companion object {
        public fun schema(): ZodUndefined = ZodUndefined()
    }

    override fun safeParse(input: Any?): ZodResult<Any?> {
        // In Kotlin/JS, undefined is typically represented differently, but for multiplatform
        // compatibility both null and a missing property are treated as undefined.
        if (input != null && input !== ZodAbsent) {
            return ZodResult.Failure(typeError("undefined", input))
        }
        return ZodResult.Success(null)
    }
}
