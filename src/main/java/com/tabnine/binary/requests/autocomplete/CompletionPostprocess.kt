package com.tabnine.binary.requests.autocomplete

import com.tabnine.general.CompletionKind

fun postprocess(request: AutocompleteRequest, result: AutocompleteResponse, tabSize: Int) {
    val resultsSubset = result.results.filter { it.completion_kind == CompletionKind.Snippet }
    if (resultsSubset.isEmpty()) {
        return
    }
    val tabsInSpaces = " ".repeat(tabSize)
    resultsSubset.forEach { it.new_prefix = it.new_prefix.replace("\t", tabsInSpaces) }

    val requestIndentation = lastLineIndentation(request.before, tabsInSpaces) ?: return
    if (requestIndentation == 0) return

    val regex = constructRegex(requestIndentation)
    resultsSubset.forEach { entry ->
        calculateTrimmingIndex(entry.new_prefix, regex)?.let { entry.new_prefix = entry.new_prefix.take(it) }
    }
}

/**
 * Finds the amount of spaces or tabs in the last line of the given `value`,
 * returning `null` if `value` has no newlines, or the last line is not whitespaces only.
 */
fun lastLineIndentation(value: String, tabsInSpaces: String): Int? {
    val lastLineStartIndex = value.lastIndexOf('\n')
    if (lastLineStartIndex == -1) return null

    val lastLine = value.substring(lastLineStartIndex + 1).replace("\t", tabsInSpaces)
    if (lastLine.isBlank()) {
        return lastLine.length
    }

    return null
}

/**
 * Constructs a regex which accepts a \n followed by at most `indentation - 1` spaces,
 * followed by any text or another \n.
 */
fun constructRegex(indentation: Int): Regex {
    val upperLimit = indentation - 1
    return Regex("""^ {0,$upperLimit}[\w\n]+""", RegexOption.MULTILINE)
}

/**
 * Finds the first match of the given `regex` in `result`, *after* the first newline appearance
 */
fun calculateTrimmingIndex(result: String, regex: Regex): Int? {
    val indexOfFirstNewline = result.indexOf('\n')
    if (indexOfFirstNewline < 0) return null
    return regex.find(result, indexOfFirstNewline + 1)?.let { it.range.first - 1 }
}
