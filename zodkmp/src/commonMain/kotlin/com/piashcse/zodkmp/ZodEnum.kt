package com.piashcse.zodkmp

/**
 * Schema for validating enum values.
 */
public class ZodEnum<T> private constructor(private val values: Set<T>, private val coerceByEnumName: Boolean) : ZodSchema<T> {
    internal fun enumValues(): Set<T> = values
    public companion object {
        public fun <T> schema(vararg values: T): ZodEnum<T> = ZodEnum(values.toSet(), false)

        public fun <T> schema(values: Collection<T>): ZodEnum<T> = ZodEnum(values.toSet(), false)

        public fun <T> schema(values: Collection<T>, coerceByEnumName: Boolean): ZodEnum<T> = ZodEnum(values.toSet(), coerceByEnumName)
    }

    override fun safeParse(input: Any?): ZodResult<T> {
        val matched =
            when {
                values.contains(input) -> {
                    @Suppress("UNCHECKED_CAST")
                    input as T
                }
                coerceByEnumName && input is String ->
                    values.firstOrNull { (it as? Enum<*>)?.name == input }
                else -> null
            }

        if (matched == null) {
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_ENUM_VALUE,
                        "Expected enum value, received $input. Valid values: ${values.joinToString(", ")}",
                    ),
                ),
            )
        }

        return ZodResult.Success(matched)
    }
}
