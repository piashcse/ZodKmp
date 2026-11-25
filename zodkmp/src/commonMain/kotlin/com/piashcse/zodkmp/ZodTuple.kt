package com.piashcse.zodkmp

/**
 * Schema for validating tuples (fixed-length arrays with a schema for each position).
 */
public class ZodTuple private constructor(private val elementSchemas: List<ZodSchema<*>>) : ZodSchema<List<Any?>> {
    internal fun elements(): List<ZodSchema<*>> = elementSchemas
    public companion object {
        public fun schema(vararg schemas: ZodSchema<*>): ZodTuple = ZodTuple(schemas.toList())

        public fun schema(schemas: List<ZodSchema<*>>): ZodTuple = ZodTuple(schemas)
    }

    override fun safeParse(input: Any?): ZodResult<List<Any?>> {
        val listInput =
            input.asListOrNull()
                ?: return ZodResult.Failure(typeError("tuple (array)", input))

        if (listInput.size != elementSchemas.size) {
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.TOO_SMALL,
                        "Tuple must have exactly ${elementSchemas.size} element(s), received ${listInput.size}",
                    ),
                ),
            )
        }

        val parsedElements = mutableListOf<Any?>()
        val issues = mutableListOf<ZodIssue>()

        for ((index, element) in listInput.withIndex()) {
            when (val elementResult = elementSchemas[index].safeParse(element)) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure ->
                    issues.addAll(
                        elementResult.error.issues.prepend(ZodPathSegment.Index(index)),
                    )
            }
        }

        return if (issues.isEmpty()) ZodResult.Success(parsedElements) else ZodResult.Failure(ZodError(issues))
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<List<Any?>> {
        val listInput =
            input.asListOrNull()
                ?: return ZodResult.Failure(typeError("tuple (array)", input))

        if (listInput.size != elementSchemas.size) {
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.TOO_SMALL,
                        "Tuple must have exactly ${elementSchemas.size} element(s), received ${listInput.size}",
                    ),
                ),
            )
        }

        val parsedElements = mutableListOf<Any?>()
        val issues = mutableListOf<ZodIssue>()

        for ((index, element) in listInput.withIndex()) {
            when (val elementResult = elementSchemas[index].safeParseSuspend(element)) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure ->
                    issues.addAll(
                        elementResult.error.issues.prepend(ZodPathSegment.Index(index)),
                    )
            }
        }

        return if (issues.isEmpty()) ZodResult.Success(parsedElements) else ZodResult.Failure(ZodError(issues))
    }
}
