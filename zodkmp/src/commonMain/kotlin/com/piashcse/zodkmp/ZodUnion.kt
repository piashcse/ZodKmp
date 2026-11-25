package com.piashcse.zodkmp

/**
 * Schema for validating union types (accepts values that match any of the provided schemas).
 */
class ZodUnion private constructor(
    private val schemas: List<ZodSchema<*>>
) : ZodSchema<Any?> {
    companion object {
        fun schema(vararg schemas: ZodSchema<*>): ZodUnion = ZodUnion(schemas.toList())
        fun schema(schemas: List<ZodSchema<*>>): ZodUnion = ZodUnion(schemas)
    }

    override fun safeParse(input: Any?): ZodResult<Any?> {
        val errors = mutableListOf<String>()
        for ((index, schema) in schemas.withIndex()) {
            when (val result = schema.safeParse(input)) {
                is ZodResult.Success -> return ZodResult.Success(result.data)
                is ZodResult.Failure -> errors.add("Option $index: ${result.error.errors.joinToString(", ")}")
            }
        }
        return ZodResult.Failure(
            ZodError(
                ZodIssue(
                    ZodIssueCode.INVALID_UNION,
                    "Union validation failed: ${errors.joinToString("; ")}"
                )
            )
        )
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<Any?> {
        val errors = mutableListOf<String>()
        for ((index, schema) in schemas.withIndex()) {
            when (val result = schema.safeParseSuspend(input)) {
                is ZodResult.Success -> return ZodResult.Success(result.data)
                is ZodResult.Failure -> errors.add("Option $index: ${result.error.errors.joinToString(", ")}")
            }
        }
        return ZodResult.Failure(
            ZodError(
                ZodIssue(
                    ZodIssueCode.INVALID_UNION,
                    "Union validation failed: ${errors.joinToString("; ")}"
                )
            )
        )
    }
}
