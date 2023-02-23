package com.tabnine.binary.fetch

import java.util.concurrent.atomic.AtomicBoolean

val once = AtomicBoolean(false)

class OnPremBinaries {
    fun path() {
        this.javaClass.getResourceAsStream("")
    }
}