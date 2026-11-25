package com.piashcse.zodkmp

/**
 * Schema that accepts any value and returns it unchanged.
 */
class ZodAny private constructor() : ZodSchema<Any?> {
    companion object {
        fun schema(): ZodAny = ZodAny()
    }

    override fun safeParse(input: Any?): ZodResult<Any?> =
        ZodResult.Success(if (input === ZodAbsent) null else input)
}

/**
 * Schema that accepts any value without type information (behaviourally identical to [ZodAny]).
 */
class ZodUnknown private constructor() : ZodSchema<Any?> {
    companion object {
        fun schema(): ZodUnknown = ZodUnknown()
    }

    override fun safeParse(input: Any?): ZodResult<Any?> =
        ZodResult.Success(if (input === ZodAbsent) null else input)
}

/**
 * Schema that rejects every value.
 */
class ZodNever private constructor() : ZodSchema<Nothing> {
    companion object {
        fun schema(): ZodNever = ZodNever()
    }

    override fun safeParse(input: Any?): ZodResult<Nothing> =
        ZodResult.Failure(
            ZodError(
                ZodIssue(
                    ZodIssueCode.INVALID_TYPE,
                    "Expected never, received ${input.typeName()}",
                    expected = "never",
                    received = input.typeName()
                )
            )
        )
}

/**
 * Schema that only accepts null/undefined input and returns [Unit], mirroring Zod's `void`.
 */
class ZodVoid private constructor() : ZodSchema<Unit> {
    companion object {
        fun schema(): ZodVoid = ZodVoid()
    }

    override fun safeParse(input: Any?): ZodResult<Unit> {
        if (input != null && input !== ZodAbsent) {
            return ZodResult.Failure(typeError("void", input))
        }
        return ZodResult.Success(Unit)
    }
}
