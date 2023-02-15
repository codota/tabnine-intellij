package com.tabnine.psi.resolver

import com.intellij.lang.Language
import com.intellij.psi.PsiFile

fun getResolver(psiFile: PsiFile): ImportsResolver? {
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
