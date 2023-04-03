package com.tabnine.psi.importsResolver

import com.intellij.lang.Language
import com.intellij.psi.PsiFile

fun getImportsResolver(psiFile: PsiFile): ImportsResolver? {
    return when (psiFile.language) {
        Language.findLanguageByID("JAVA") -> {
            JavaImportsResolver()
        }
        Language.findLanguageByID("TypeScript") -> {
            TypeScriptImportsResolver()
        }
        Language.findLanguageByID("ECMAScript 6") -> {
            JavaScriptImportsResolver()
        }
        else -> null
    }
}
