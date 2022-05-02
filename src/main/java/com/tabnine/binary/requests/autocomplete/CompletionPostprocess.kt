package com.tabnine.binary.requests.autocomplete

fun postprocess(request: AutocompleteRequest, result: AutocompleteResponse, tabSize: Int) {
    result.results.forEach { it.new_prefix = it.new_prefix.replace("\t", " ".repeat(tabSize)) }

    val requestIndentation = lastLineIndentation(request.before.replace("\t", " ".repeat(tabSize))) ?: return
    if (requestIndentation == 0) return

    val regex = constructRegex(requestIndentation)
    result.results.forEach { entry ->
        val calculateTrimmingIndex = calculateTrimmingIndex(entry.new_prefix, regex)
        calculateTrimmingIndex?.let { entry.new_prefix = entry.new_prefix.take(it) }
    }
}

fun lastLineIndentation(value: String): Int? {
    try {
        val lastLine = value.lines().last()
        if (lastLine.isBlank()) {
            return lastLine.length
        }
    } catch (e: java.util.NoSuchElementException) {
        // do nothing
    }
    return null
}

fun constructRegex(indentation: Int): Regex {
    val upperLimit = indentation - 1
    return Regex("""^ {0,$upperLimit}[\w\n]+""", RegexOption.MULTILINE)
}

fun calculateTrimmingIndex(result: String, regex: Regex): Int? {
    val indexOfFirstNewline = result.indexOf("\n")
    if (indexOfFirstNewline < 0) return null
    return regex.find(result, indexOfFirstNewline + 1)?.let { it.range.first - 1 }
}
