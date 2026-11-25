package com.piashcse.zodkmp

/**
 * A custom refinement definition.
 */
data class ZodRefinement<T>(
    val refinement: (T) -> Boolean,
    val errorMessage: (T) -> String = { "Invalid value: $it" }
)

fun <T> ZodSchema<T>.refine(
    refinement: (T) -> Boolean,
    errorMessage: (T) -> String = { "Invalid value: $it" }
): ZodSchemaWithRefinement<T> {
    return ZodSchemaWithRefinement(this, listOf(ZodRefinement(refinement, errorMessage)))
}

/**
 * Wrapper schema for adding refinements to schemas that do not expose a typed `refine`
 * method of their own (string, number, array and object schemas do).
 */
class ZodSchemaWithRefinement<T>(
    private val baseSchema: ZodSchema<T>,
    private val refinements: List<ZodRefinement<T>>
) : ZodSchema<T> {

    override fun safeParse(input: Any?): ZodResult<T> {
        val baseResult = baseSchema.safeParse(input)
        if (baseResult is ZodResult.Failure) {
            return baseResult
        }

        @Suppress("UNCHECKED_CAST")
        val value = (baseResult as ZodResult.Success<T>).data
        return applyRefinements(value)
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T> {
        val baseResult = baseSchema.safeParseSuspend(input)
        if (baseResult is ZodResult.Failure) {
            return baseResult
        }

        @Suppress("UNCHECKED_CAST")
        val value = (baseResult as ZodResult.Success<T>).data
        return applyRefinements(value)
    }

    private fun applyRefinements(value: T): ZodResult<T> {
        val issues = mutableListOf<ZodIssue>()
        for (refinement in refinements) {
            if (!refinement.refinement(value)) {
                issues.add(issue(ZodIssueCode.CUSTOM, refinement.errorMessage(value)))
            }
        }
        return if (issues.isEmpty()) ZodResult.Success(value) else ZodResult.Failure(ZodError(issues))
    }
}
