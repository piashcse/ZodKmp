package com.piashcse.zodkmp

/**
 * Main entry point for ZodKmp validation schemas
 */
object Zod {
    fun string() = ZodString.schema()
    fun number() = ZodNumber.schema()
    fun boolean() = ZodBoolean.schema()
    
    inline fun <reified T> objectSchema(
        shapeBuilder: ZodObjectShapeBuilder.() -> Unit,
        noinline parser: (Map<String, Any?>) -> T
    ) = ZodObjectSchema.build(shapeBuilder, parser)
}