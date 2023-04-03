package com.tabnine.psi

import java.net.URI

data class Position(val line: Int, val column: Int)

data class Range(val start: Position, val end: Position)

data class LocationLink(val targetUri: URI, val targetRange: Range)
