package com.piashcse.zodkmp

/**
 * A suspend (async) refinement definition.
 */
data class SuspendRefinement<T>(
    val refinement: suspend (T) -> Boolean,
    val errorMessage: suspend (T) -> String = { "Invalid value: $it" }
)

/**
 * Schema wrapper that runs suspend (async) refinements after validating against a base schema.
 *
 * Because suspend functions cannot run inside synchronous [parse]/[safeParse] on
 * Kotlin Multiplatform, those methods throw [IllegalStateException]; use
 * [safeParseSuspend]/[parseSuspend] instead.
 */
class ZodSuspendRefinement<T> internal constructor(
    private val baseSchema: ZodSchema<T>,
    private val refinements: List<SuspendRefinement<T>>
) : ZodSchema<T> {
    override fun parse(input: Any?): T {
        throw IllegalStateException("This schema contains suspend refinements. Use parseSuspend() instead.")
    }
    
    override fun safeParse(input: Any?): ZodResult<T> {
        throw IllegalStateException("This schema contains suspend refinements. Use safeParseSuspend() instead.")
    }
    
    override suspend fun safeParseSuspend(input: Any?): ZodResult<T> {
        val baseResult = baseSchema.safeParse(input)
        if (baseResult is ZodResult.Failure) {
            return baseResult
        }
        
        @Suppress("UNCHECKED_CAST")
        val value = (baseResult as ZodResult.Success<T>).data
        
        val errors = mutableListOf<String>()
        for (refinement in refinements) {
            if (!refinement.refinement(value)) {
                errors.add(refinement.errorMessage(value))
            }
        }
        
        return if (errors.isEmpty()) {
            ZodResult.Success(value)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}

fun <T> ZodSchema<T>.refineSuspend(
    refinement: suspend (T) -> Boolean,
    errorMessage: suspend (T) -> String = { "Invalid value: $it" }
): ZodSuspendRefinement<T> = ZodSuspendRefinement(this, listOf(SuspendRefinement(refinement, errorMessage)))
