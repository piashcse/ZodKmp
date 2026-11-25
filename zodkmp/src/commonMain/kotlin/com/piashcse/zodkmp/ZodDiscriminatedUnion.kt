package com.piashcse.zodkmp

/**
 * Schema for discriminated unions, matching Zod's `z.discriminatedUnion("type", [...])`.
 *
 * The input must be an object with a discriminator key whose value selects one of the
 * provided object schemas. Every option must declare a [ZodLiteral] for the discriminator
 * key, and discriminator values must be unique. Unknown or missing discriminators fail
 * without attempting to parse the other options.
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
        ): ZodDiscriminatedUnion<T> {
            require(options.isNotEmpty()) { "discriminatedUnion requires at least one option" }

            val seen = mutableListOf<Pair<Any?, Int>>()
            options.forEachIndexed { index, option ->
                val literal = option.shape[discriminator] as? ZodLiteral<*>
                    ?: error(
                        "Invalid discriminated union option at index $index: " +
                            "missing ZodLiteral for discriminator '$discriminator'"
                    )
                val duplicate = seen.any { (value, _) -> zodEquals(value, literal.literalValue) }
                require(!duplicate) {
                    "Duplicate discriminator value '${literal.literalValue}' at index $index"
                }
                seen.add(literal.literalValue to index)
            }

            return ZodDiscriminatedUnion(discriminator, options.toList(), emptyList())
        }
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
            ?: return ZodResult.Failure(
                ZodError("Expected object with discriminator key '$discriminator', received ${input.typeName()}")
            )

        if (!mapInput.containsKey(discriminator)) {
            return ZodResult.Failure(ZodError("Missing discriminator key '$discriminator'"))
        }

        val discriminatorValue = mapInput[discriminator]
        val targetOption = options.firstOrNull { option ->
            val literal = option.shape[discriminator] as? ZodLiteral<*>
            literal != null && zodEquals(literal.literalValue, discriminatorValue)
        } ?: return ZodResult.Failure(
            ZodError(
                "Invalid discriminator value '$discriminatorValue' for key '$discriminator'. " +
                    "Valid values: ${options.joinToString(", ") { option ->
                        ((option.shape[discriminator] as? ZodLiteral<*>)?.literalValue ?: "?").toString()
                    }}"
            )
        )

        val result = targetOption.safeParse(input)
        if (result is ZodResult.Failure) {
            return ZodResult.Failure(result.error)
        }

        @Suppress("UNCHECKED_CAST")
        val data = (result as ZodResult.Success<*>).data as T

        for (validation in validations) {
            validation(data)?.let { return ZodResult.Failure(it) }
        }

        return ZodResult.Success(data)
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T> {
        val mapInput = input as? Map<*, *>
            ?: return ZodResult.Failure(
                ZodError("Expected object with discriminator key '$discriminator', received ${input.typeName()}")
            )

        if (!mapInput.containsKey(discriminator)) {
            return ZodResult.Failure(ZodError("Missing discriminator key '$discriminator'"))
        }

        val discriminatorValue = mapInput[discriminator]
        val targetOption = options.firstOrNull { option ->
            val literal = option.shape[discriminator] as? ZodLiteral<*>
            literal != null && zodEquals(literal.literalValue, discriminatorValue)
        } ?: return ZodResult.Failure(
            ZodError(
                "Invalid discriminator value '$discriminatorValue' for key '$discriminator'. " +
                    "Valid values: ${options.joinToString(", ") { option ->
                        ((option.shape[discriminator] as? ZodLiteral<*>)?.literalValue ?: "?").toString()
                    }}"
            )
        )

        val result = targetOption.safeParseSuspend(input)
        if (result is ZodResult.Failure) {
            return ZodResult.Failure(result.error)
        }

        @Suppress("UNCHECKED_CAST")
        val data = (result as ZodResult.Success<*>).data as T

        for (validation in validations) {
            validation(data)?.let { return ZodResult.Failure(it) }
        }

        return ZodResult.Success(data)
    }
}
