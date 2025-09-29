package com.piashcse.zodkmp

/**
 * Schema extension for adding custom refinements to any schema
 */
data class ZodRefinement<T>(
    val schema: ZodSchema<T>,
    val refinement: (T) -> Boolean,
    val errorMessage: (T) -> String = { "Invalid value: $it" }
)

fun <T> ZodSchema<T>.refine(
    refinement: (T) -> Boolean, 
    errorMessage: (T) -> String = { "Invalid value: $it" }
): ZodSchemaWithRefinement<T> {
    return ZodSchemaWithRefinement(this, listOf(ZodRefinement(this, refinement, errorMessage)))
}

/**
 * Wrapper schema that includes refinements
 */
class ZodSchemaWithRefinement<T>(
    private val baseSchema: ZodSchema<T>,
    private val refinements: List<ZodRefinement<T>>
) : ZodSchema<T> {
    
    override fun parse(input: Any?): T {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<T> {
        // First validate against base schema
        val baseResult = baseSchema.safeParse(input)
        if (baseResult is ZodResult.Failure) {
            return baseResult
        }
        
        @Suppress("UNCHECKED_CAST")
        val value = (baseResult as ZodResult.Success<T>).data
        
        // Then apply refinements
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