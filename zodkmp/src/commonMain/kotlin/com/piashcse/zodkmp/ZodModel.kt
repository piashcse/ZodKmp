package com.piashcse.zodkmp

/**
 * Marks a data class for schema generation by the `zodkmp-ksp` processor.
 *
 * For `data class User(val name: String, val age: Int)` the processor generates:
 *
 * ```kotlin
 * public object UserZod {
 *     public val schema: ZodObjectSchema<User>
 * }
 * ```
 *
 * Supported property types: `String`, `Int`, `Long`, `Double`, `Float`, `Boolean`,
 * enums, other `@ZodModel` classes, nullable variants, and `List<T>` of supported types.
 * Unknown types fall back to `Zod.any()` and a KSP warning.
 *
 * @property strict when true the generated schema rejects unknown keys.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class ZodModel(public val strict: Boolean = false)
