package com.piashcse.zodkmp

/**
 * Schema for validating intersection types (a value must satisfy both schemas).
 *
 * For two object schemas prefer [Zod.intersection]'s object overload, which merges the
 * shapes into a single object schema. This generic schema returns both parsed values.
 */
public class ZodIntersection<T, U> private constructor(
    private val leftSchema: ZodSchema<T>,
    private val rightSchema: ZodSchema<U>,
) : ZodSchema<Pair<T, U>> {
    internal fun left(): ZodSchema<T> = leftSchema

    internal fun right(): ZodSchema<U> = rightSchema

    public companion object {
        public fun <T, U> schema(
            leftSchema: ZodSchema<T>,
            rightSchema: ZodSchema<U>,
        ): ZodIntersection<T, U> = ZodIntersection(leftSchema, rightSchema)
    }

    override fun safeParse(input: Any?): ZodResult<Pair<T, U>> {
        val leftResult = leftSchema.safeParse(input)
        val rightResult = rightSchema.safeParse(input)
        return combine(leftResult, rightResult)
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<Pair<T, U>> {
        val leftResult = leftSchema.safeParseSuspend(input)
        val rightResult = rightSchema.safeParseSuspend(input)
        return combine(leftResult, rightResult)
    }

    private fun combine(
        leftResult: ZodResult<T>,
        rightResult: ZodResult<U>,
    ): ZodResult<Pair<T, U>> {
        val issues = mutableListOf<ZodIssue>()
        if (leftResult is ZodResult.Failure) {
            issues.add(
                ZodIssue(
                    ZodIssueCode.INVALID_INTERSECTION,
                    "Left schema validation failed: ${leftResult.error.errors.joinToString(", ")}",
                ),
            )
        }
        if (rightResult is ZodResult.Failure) {
            issues.add(
                ZodIssue(
                    ZodIssueCode.INVALID_INTERSECTION,
                    "Right schema validation failed: ${rightResult.error.errors.joinToString(", ")}",
                ),
            )
        }
        if (issues.isNotEmpty()) {
            return ZodResult.Failure(ZodError(issues))
        }

        @Suppress("UNCHECKED_CAST")
        val pair =
            Pair(
                (leftResult as ZodResult.Success<T>).data,
                (rightResult as ZodResult.Success<U>).data,
            )
        return ZodResult.Success(pair)
    }
}
