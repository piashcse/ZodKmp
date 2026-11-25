package com.piashcse.zodkmp

/**
 * Schema that defers construction of the wrapped schema until it is used.
 * This makes recursive schemas possible:
 *
 * ```kotlin
 * lateinit var category: ZodSchema<Category>
 * category = Zod.lazy {
 *     Zod.objectSchema<Category>({
 *         string("name")
 *         field("children", Zod.array(category))
 *     }) { map -> ... }
 * }
 * ```
 */
public class ZodLazy<T> internal constructor(
    private val schemaFactory: () -> ZodSchema<T>,
) : ZodSchema<T> {
    internal fun resolve(): ZodSchema<T> = schemaFactory()

    override fun safeParse(input: Any?): ZodResult<T> = schemaFactory().safeParse(input)

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T> = schemaFactory().safeParseSuspend(input)
}

public fun <T> Zod.lazy(schemaFactory: () -> ZodSchema<T>): ZodLazy<T> = ZodLazy(schemaFactory)
