package com.piashcse.zodkmp

/**
 * Schema that accepts any value and returns it unchanged.
 */
public class ZodAny private constructor() : ZodSchema<Any?> {
    public companion object {
        public fun schema(): ZodAny = ZodAny()
    }

    override fun safeParse(input: Any?): ZodResult<Any?> = ZodResult.Success(if (input === ZodAbsent) null else input)
}

/**
 * Schema that accepts any value without type information (behaviourally identical to [ZodAny]).
 */
public class ZodUnknown private constructor() : ZodSchema<Any?> {
    public companion object {
        public fun schema(): ZodUnknown = ZodUnknown()
    }

    override fun safeParse(input: Any?): ZodResult<Any?> = ZodResult.Success(if (input === ZodAbsent) null else input)
}

/**
 * Schema that rejects every value.
 */
public class ZodNever private constructor() : ZodSchema<Nothing> {
    public companion object {
        public fun schema(): ZodNever = ZodNever()
    }

    override fun safeParse(input: Any?): ZodResult<Nothing> =
        ZodResult.Failure(
            ZodError(
                ZodIssue(
                    ZodIssueCode.INVALID_TYPE,
                    "Expected never, received ${input.typeName()}",
                    expected = "never",
                    received = input.typeName(),
                ),
            ),
        )
}

/**
 * Schema that only accepts null/undefined input and returns [Unit], mirroring Zod's `void`.
 */
public class ZodVoid private constructor() : ZodSchema<Unit> {
    public companion object {
        public fun schema(): ZodVoid = ZodVoid()
    }

    override fun safeParse(input: Any?): ZodResult<Unit> {
        if (input != null && input !== ZodAbsent) {
            return ZodResult.Failure(typeError("void", input))
        }
        return ZodResult.Success(Unit)
    }
}
