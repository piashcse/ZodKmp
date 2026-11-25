package com.piashcse.zodkmp

/**
 * Schema for validating literal values.
 */
class ZodLiteral<T> private constructor(
    private val value: T
) : ZodSchema<T> {
    companion object {
        fun <T> schema(value: T): ZodLiteral<T> = ZodLiteral(value)

        fun <T> schema(vararg values: T): ZodSchema<T> {
            if (values.isEmpty()) {
                @Suppress("UNCHECKED_CAST")
                return ZodNever.schema() as ZodSchema<T>
            }
            return if (values.size == 1) {
                ZodLiteral.schema(values[0])
            } else {
                @Suppress("UNCHECKED_CAST")
                ZodUnion.schema(*values.map { ZodLiteral.schema(it) as ZodSchema<*> }.toTypedArray()) as ZodSchema<T>
            }
        }
    }

    val literalValue: T get() = value

    override fun safeParse(input: Any?): ZodResult<T> {
        if (!zodEquals(input, value)) {
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_LITERAL,
                        "Expected literal $value, received $input",
                        expected = value.toString(),
                        received = input.toString()
                    )
                )
            )
        }
        return ZodResult.Success(value)
    }
}
