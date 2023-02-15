package com.tabnine.psi.resolver

import com.intellij.psi.PsiElement

class TypeScriptImportsResolver : ImportsResolver() {
    override fun potentialElementsPredicate(element: PsiElement): Boolean {
        return element.text.contains("import")
    }
}
