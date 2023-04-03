package com.tabnine.psi.importsResolver

import com.intellij.psi.PsiElement

class JavaScriptImportsResolver : ImportsResolver() {
    override fun potentialElementsPredicate(element: PsiElement): Boolean {
        return element.text.contains("import")
    }
}
