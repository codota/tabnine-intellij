package com.tabnineCommon.chat.commandHandlers.utils

object StringCaseConverter {
    private val camelCaseRegex = Regex("(?<=.)[A-Z]")
    private val snakeCaseRegex = Regex("_([a-z])")

    fun toSnakeCase(str: String): String {
        return str.replace(camelCaseRegex, "_$0").toLowerCase()
    }

    fun toCamelCase(str: String): String {
        return str.replace(snakeCaseRegex) { it.value.last().toUpperCase().toString() }
    }
}
