package com.piashcse.zodkmp

/**
 * Schema for discriminated unions, matching Zod's `z.discriminatedUnion("type", [...])`.
 *
 * The input must be an object with a discriminator key whose value selects one of the
 * provided object schemas. Each option should declare a `ZodLiteral` for the discriminator
 * key; options without a plain literal discriminator are matched by attempting to parse.
 *
 * This is the idiomatic way to map into a Kotlin `sealed class` where every option's
 * parser produces a subtype of the sealed base type.
 */
class ZodDiscriminatedUnion<T> private constructor(
    private val discriminator: String,
    private val options: List<ZodObjectSchema<*>>,
    private val validations: List<(T) -> ZodError?>
) : ZodSchema<T> {
    companion object {
        fun <T> schema(
            discriminator: String,
            options: List<ZodObjectSchema<out T>>
        ): ZodDiscriminatedUnion<T> = ZodDiscriminatedUnion(discriminator, options.toList(), emptyList())
    }
    
    override fun parse(input: Any?): T {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<T> {
        val mapInput = input as? Map<*, *>
        if (mapInput == null) {
            return ZodResult.Failure(ZodError("Expected object with discriminator key '$discriminator'"))
        }
        
        val discriminatorValue = mapInput[discriminator]
        
        // 1. Prefer an option whose discriminator literal matches the input value.
        val literalOption = if (discriminatorValue != null) {
            options.firstOrNull { option ->
                (option.shape[discriminator] as? ZodLiteral<*>)?.literalValue == discriminatorValue
            }
        } else {
            null
        }
        
        val targetOption = literalOption ?: run {
            // 2. Fallback: try each option until one parses successfully.
            val errors = mutableListOf<String>()
            for ((index, option) in options.withIndex()) {
                val result = option.safeParse(input)
                when (result) {
                    is ZodResult.Success -> return ZodResult.Success(result.data as T)
                    is ZodResult.Failure -> {
                        errors.add("Option $index: ${result.error.errors.joinToString(", ")}")
                    }
                }
            }
            if (discriminatorValue == null) {
                return ZodResult.Failure(ZodError("Missing discriminator key '$discriminator'"))
            }
            return ZodResult.Failure(ZodError("Invalid discriminator value '$discriminatorValue'. ${errors.joinToString("; ")}"))
        }
        
        val result = targetOption.safeParse(input)
        if (result is ZodResult.Failure) {
            return ZodResult.Failure(result.error)
        }
        
        @Suppress("UNCHECKED_CAST")
        val data = (result as ZodResult.Success<*>).data as T
        
        for (validation in validations) {
            val error = validation(data)
            if (error != null) {
                return ZodResult.Failure(error)
            }
        }
        
        return ZodResult.Success(data)
    }
}
