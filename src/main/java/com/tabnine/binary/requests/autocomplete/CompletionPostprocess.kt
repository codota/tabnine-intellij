package com.tabnine.binary.requests.autocomplete

import com.tabnine.general.CompletionKind

fun postprocess(request: AutocompleteRequest, result: AutocompleteResponse, tabSize: Int) {
    val resultsSubset = result.results.filter { it.completion_kind == CompletionKind.Snippet }
    if (resultsSubset.isEmpty()) {
        return
    }

    resultsSubset.forEach { it.new_prefix = it.new_prefix.replace("\t", " ".repeat(tabSize)) }

    val requestIndentation = lastLineIndentation(request.before, tabSize) ?: return
    if (requestIndentation == 0) return

    val regex = constructRegex(requestIndentation)
    resultsSubset.forEach { entry ->
        val calculateTrimmingIndex = calculateTrimmingIndex(entry.new_prefix, regex)
        calculateTrimmingIndex?.let { entry.new_prefix = entry.new_prefix.take(it) }
    }
}

/**
 * Finds the amount of spaces or tabs in the last line of the given `value`,
 * returning `null` if `value` has no newlines, or the last line is not whitespaces only.
 */
fun lastLineIndentation(value: String, tabSize: Int): Int? {
    try {
        val lastLineStartIndex = value.lastIndexOf('\n') + 1

        val lastLine = value.substring(lastLineStartIndex).replace("\t", " ".repeat(tabSize))
        if (lastLine.isBlank()) {
            return lastLine.length
        }
    } catch (e: java.util.NoSuchElementException) {
        // do nothing
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
