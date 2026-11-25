package com.piashcse.zodkmp

/**
 * Schema for validating undefined values (represented as `null` or a missing property in Kotlin).
 */
class ZodUndefined internal constructor(
    private val validations: List<(Any?) -> ZodError?>
) : ZodSchema<Any?> {
    companion object {
        fun schema(): ZodUndefined = ZodUndefined(emptyList())
    }

    override fun parse(input: Any?): Any? {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }

    override fun safeParse(input: Any?): ZodResult<Any?> {
        // In Kotlin/JS, undefined is typically represented differently, but for multiplatform
        // compatibility both null and a missing property are treated as undefined.
        if (input != null && input !== ZodAbsent) {
            return ZodResult.Failure(ZodError("Expected undefined, received ${input.typeName()}"))
        }

        val errors = mutableListOf<String>()
        for (validation in validations) {
            validation(null)?.let { errors.addAll(it.errors) }
        }

        return if (errors.isEmpty()) {
            ZodResult.Success(null)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}
