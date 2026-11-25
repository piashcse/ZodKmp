package com.piashcse.zodkmp

/**
 * Schema that validates that the input is an instance of a specific type.
 * Created via `Zod.instanceOf<T>()` (reified, no reflection).
 */
public class ZodInstanceOf<T : Any>
@PublishedApi
internal constructor(private val isInstance: (Any?) -> Boolean) : ZodSchema<T> {
    public companion object {
        public inline fun <reified T : Any> schema(): ZodInstanceOf<T> = ZodInstanceOf({ it is T })
    }

    override fun safeParse(input: Any?): ZodResult<T> {
        if (!isInstance(input)) {
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_TYPE,
                        "Expected instance of type, received ${input.typeName()}",
                        received = input.typeName(),
                    ),
                ),
            )
        }

        @Suppress("UNCHECKED_CAST")
        return ZodResult.Success(input as T)
    }
}
