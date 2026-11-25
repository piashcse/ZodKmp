package com.piashcse.zodkmp

/**
 * Ready-made error maps for [ZodConfig.errorMap].
 *
 * Each locale translates the built-in issue codes; returning `null` falls back to the
 * original message (used for [ZodIssueCode.CUSTOM], where the user supplied the text).
 *
 * ```kotlin
 * ZodConfig.errorMap = ZodLocales.spanish
 * ```
 */
public object ZodLocales {
    /** Keeps the built-in English messages. */
    public val english: (ZodIssue) -> String? = { null }

    public val spanish: (ZodIssue) -> String? = { issue -> translate(issue, SPANISH) }

    public val french: (ZodIssue) -> String? = { issue -> translate(issue, FRENCH) }

    public val bengali: (ZodIssue) -> String? = { issue -> translate(issue, BENGALI) }

    private fun translate(
        issue: ZodIssue,
        messages: Map<ZodIssueCode, String>,
    ): String? {
        val template = messages[issue.code] ?: return null
        return template
            .replace("{expected}", issue.expected ?: "")
            .replace("{received}", issue.received ?: "")
    }

    private val SPANISH =
        mapOf(
            ZodIssueCode.REQUIRED to "Campo obligatorio",
            ZodIssueCode.INVALID_TYPE to "Tipo no válido: se esperaba {expected}",
            ZodIssueCode.INVALID_FORMAT to "Formato no válido",
            ZodIssueCode.INVALID_LITERAL to "Valor literal no válido",
            ZodIssueCode.INVALID_UNION to "El valor no coincide con ninguna opción",
            ZodIssueCode.INVALID_INTERSECTION to "El valor no satisface todos los esquemas",
            ZodIssueCode.INVALID_ENUM_VALUE to "Valor no permitido",
            ZodIssueCode.INVALID_KEY to "Clave no válida",
            ZodIssueCode.UNRECOGNIZED_KEYS to "Hay claves no reconocidas",
            ZodIssueCode.TOO_SMALL to "El valor es demasiado pequeño",
            ZodIssueCode.TOO_BIG to "El valor es demasiado grande",
            ZodIssueCode.NOT_MULTIPLE_OF to "El valor no es un múltiplo válido",
            ZodIssueCode.NOT_FINITE to "El valor debe ser finito",
        )

    private val FRENCH =
        mapOf(
            ZodIssueCode.REQUIRED to "Champ obligatoire",
            ZodIssueCode.INVALID_TYPE to "Type invalide : {expected} attendu",
            ZodIssueCode.INVALID_FORMAT to "Format invalide",
            ZodIssueCode.INVALID_LITERAL to "Valeur littérale invalide",
            ZodIssueCode.INVALID_UNION to "La valeur ne correspond à aucune option",
            ZodIssueCode.INVALID_INTERSECTION to "La valeur ne satisfait pas tous les schémas",
            ZodIssueCode.INVALID_ENUM_VALUE to "Valeur non autorisée",
            ZodIssueCode.INVALID_KEY to "Clé invalide",
            ZodIssueCode.UNRECOGNIZED_KEYS to "Clés non reconnues",
            ZodIssueCode.TOO_SMALL to "Valeur trop petite",
            ZodIssueCode.TOO_BIG to "Valeur trop grande",
            ZodIssueCode.NOT_MULTIPLE_OF to "La valeur n'est pas un multiple valide",
            ZodIssueCode.NOT_FINITE to "La valeur doit être finie",
        )

    private val BENGALI =
        mapOf(
            ZodIssueCode.REQUIRED to "আবশ্যক",
            ZodIssueCode.INVALID_TYPE to "অবৈধ ধরন: {expected} প্রত্যাশিত",
            ZodIssueCode.INVALID_FORMAT to "অবৈধ ফরম্যাট",
            ZodIssueCode.INVALID_LITERAL to "অবৈধ লিটারাল মান",
            ZodIssueCode.INVALID_UNION to "মানটি কোনো অপশনের সাথে মেলে না",
            ZodIssueCode.INVALID_INTERSECTION to "মানটি সব স্কিমা পূরণ করে না",
            ZodIssueCode.INVALID_ENUM_VALUE to "অনুমোদিত মান নয়",
            ZodIssueCode.INVALID_KEY to "অবৈধ কী",
            ZodIssueCode.UNRECOGNIZED_KEYS to "অপরিচিত কী রয়েছে",
            ZodIssueCode.TOO_SMALL to "মানটি খুব ছোট",
            ZodIssueCode.TOO_BIG to "মানটি খুব বড়",
            ZodIssueCode.NOT_MULTIPLE_OF to "মানটি বৈধ গুণিতক নয়",
            ZodIssueCode.NOT_FINITE to "মানটি সসীম হতে হবে",
        )
}
