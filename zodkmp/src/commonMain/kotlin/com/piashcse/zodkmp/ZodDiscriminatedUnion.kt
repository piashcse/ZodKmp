package com.piashcse.zodkmp

/**
 * Schema for discriminated unions, matching Zod's `z.discriminatedUnion("type", [...])`.
 *
 * The input must be an object with a discriminator key whose value selects one of the
 * provided object schemas. Every option must declare a [ZodLiteral] for the discriminator
 * key, and discriminator values must be unique. Unknown or missing discriminators fail
 * without attempting to parse the other options.
 */
public class ZodDiscriminatedUnion<T> private constructor(private val discriminator: String, private val options: List<ZodObjectSchema<*>>) : ZodSchema<T> {
    internal fun discriminatorKey(): String = discriminator

    internal fun optionSchemas(): List<ZodObjectSchema<*>> = options
    public companion object {
        public fun <T> schema(discriminator: String, options: List<ZodObjectSchema<out T>>): ZodDiscriminatedUnion<T> {
            require(options.isNotEmpty()) { "discriminatedUnion requires at least one option" }

            val seen = mutableListOf<Pair<Any?, Int>>()
            options.forEachIndexed { index, option ->
                val literal =
                    option.shape[discriminator] as? ZodLiteral<*>
                        ?: error(
                            "Invalid discriminated union option at index $index: " +
                                "missing ZodLiteral for discriminator '$discriminator'",
                        )
                require(seen.none { (value, _) -> zodEquals(value, literal.literalValue) }) {
                    "Duplicate discriminator value '${literal.literalValue}' at index $index"
                }
                seen.add(literal.literalValue to index)
            }

            return ZodDiscriminatedUnion(discriminator, options.toList())
        }
    }

    override fun safeParse(input: Any?): ZodResult<T> {
        val mapInput =
            input as? Map<*, *>
                ?: return ZodResult.Failure(
                    ZodError(
                        ZodIssue(
                            ZodIssueCode.INVALID_TYPE,
                            "Expected object with discriminator key '$discriminator', received ${input.typeName()}",
                        ),
                    ),
                )

        if (!mapInput.containsKey(discriminator)) {
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.REQUIRED,
                        "Missing discriminator key '$discriminator'",
                        path = listOf(ZodPathSegment.Property(discriminator)),
                    ),
                ),
            )
        }

        val discriminatorValue = mapInput[discriminator]
        val targetOption =
            options.firstOrNull { option ->
                val literal = option.shape[discriminator] as? ZodLiteral<*>
                literal != null && zodEquals(literal.literalValue, discriminatorValue)
            } ?: return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_UNION,
                        "Invalid discriminator value '$discriminatorValue' for key '$discriminator'. " +
                            "Valid values: ${options.joinToString(", ") { option ->
                                ((option.shape[discriminator] as? ZodLiteral<*>)?.literalValue ?: "?").toString()
                            }}",
                        path = listOf(ZodPathSegment.Property(discriminator)),
                    ),
                ),
            )

        val result = targetOption.safeParse(input)
        if (result is ZodResult.Failure) {
            return ZodResult.Failure(result.error)
        }

        @Suppress("UNCHECKED_CAST")
        return ZodResult.Success((result as ZodResult.Success<*>).data as T)
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T> {
        val mapInput =
            input as? Map<*, *>
                ?: return ZodResult.Failure(
                    ZodError(
                        ZodIssue(
                            ZodIssueCode.INVALID_TYPE,
                            "Expected object with discriminator key '$discriminator', received ${input.typeName()}",
                        ),
                    ),
                )

        if (!mapInput.containsKey(discriminator)) {
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.REQUIRED,
                        "Missing discriminator key '$discriminator'",
                        path = listOf(ZodPathSegment.Property(discriminator)),
                    ),
                ),
            )
        }

        val discriminatorValue = mapInput[discriminator]
        val targetOption =
            options.firstOrNull { option ->
                val literal = option.shape[discriminator] as? ZodLiteral<*>
                literal != null && zodEquals(literal.literalValue, discriminatorValue)
            } ?: return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_UNION,
                        "Invalid discriminator value '$discriminatorValue' for key '$discriminator'. " +
                            "Valid values: ${options.joinToString(", ") { option ->
                                ((option.shape[discriminator] as? ZodLiteral<*>)?.literalValue ?: "?").toString()
                            }}",
                        path = listOf(ZodPathSegment.Property(discriminator)),
                    ),
                ),
            )

        val result = targetOption.safeParseSuspend(input)
        if (result is ZodResult.Failure) {
            return ZodResult.Failure(result.error)
        }

        @Suppress("UNCHECKED_CAST")
        return ZodResult.Success((result as ZodResult.Success<*>).data as T)
    }
}
