package com.piashcse.zodkmp

/**
 * Schema for validating booleans.
 *
 * By default only [Boolean] values are accepted. Use [Zod.coerce] to opt into
 * accepting the strings `"true"` / `"false"`.
 */
class ZodBoolean private constructor(
    private val validations: List<(Boolean) -> ZodIssue?>,
    private val coerce: Boolean
) : ZodSchema<Boolean> {
    companion object {
        fun schema(coerce: Boolean = false): ZodBoolean = ZodBoolean(emptyList(), coerce)
    }

    fun isEqual(value: Boolean, message: String = "Boolean must be equal to $value"): ZodBoolean =
        ZodBoolean(
            validations + { bool -> if (bool != value) issue(ZodIssueCode.CUSTOM, message) else null },
            coerce
        )

    fun refine(predicate: (Boolean) -> Boolean, message: (Boolean) -> String = { "Invalid value: $it" }): ZodBoolean =
        ZodBoolean(
            validations + { value -> if (predicate(value)) null else issue(ZodIssueCode.CUSTOM, message(value)) },
            coerce
        )

    override fun safeParse(input: Any?): ZodResult<Boolean> {
        val booleanInput = when (input) {
            is Boolean -> input
            is String -> if (coerce) {
                when (input.trim().lowercase()) {
                    "true" -> true
                    "false" -> false
                    else -> null
                }
            } else {
                null
            }
            else -> null
        }

        if (booleanInput == null) {
            val hint = if (input is String && !coerce) " (use Zod.coerce.boolean() to accept \"true\"/\"false\")" else ""
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_TYPE,
                        "Expected boolean, received ${input.typeName()}$hint",
                        expected = "boolean",
                        received = input.typeName()
                    )
                )
            )
        }

        val issues = mutableListOf<ZodIssue>()
        for (validation in validations) {
            validation(booleanInput)?.let { issues.add(it) }
        }

        return if (issues.isEmpty()) ZodResult.Success(booleanInput) else ZodResult.Failure(ZodError(issues))
    }
}
