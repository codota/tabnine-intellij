package com.tabnineCommon.statusBar

// Workaround to address the issue where IJ does not render the width of the status bar icon
// correctly when the icon changes
// because it doesn't detect a change in the widget text (it renders it correctly just after
// document change occurs).
// Creating a different empty text each time will solve the problem.
class StatusBarEmptySymbolGenerator {
    companion object {
        private const val EMPTY_SYMBOL = "\u0000"
        private val EMPTY_SYMBOLS = listOf(EMPTY_SYMBOL, EMPTY_SYMBOL + EMPTY_SYMBOL)
    }

    private var currentSymbolIndex = 0
    val emptySymbol: String
        get() = EMPTY_SYMBOLS[currentSymbolIndex++ % EMPTY_SYMBOLS.size]
}
