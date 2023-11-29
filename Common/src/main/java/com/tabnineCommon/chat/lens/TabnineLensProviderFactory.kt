package com.tabnineCommon.chat.lens

import com.intellij.codeInsight.hints.InlayHintsProviderFactory
import com.intellij.codeInsight.hints.ProviderInfo
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability

class TabnineLensProviderFactory : InlayHintsProviderFactory {
    override fun getProvidersInfo(project: Project): List<ProviderInfo<out Any>> {
        if (!CapabilitiesService.getInstance().isCapabilityEnabled(Capability.TABNINE_CHAT)) {
            return emptyList()
        }
        return Language.getRegisteredLanguages().filter { shouldSupport(it) }.map { lang ->
            val supportedElementTypes = getSupportedElementTypesForLanguage(lang)
            ProviderInfo(lang, TabnineLensProvider(supportedElementTypes))
        }
    }

    private fun shouldSupport(language: Language): Boolean {
        // Define your logic to determine whether a language should be supported
        val supportedLanguages = setOf("java", "python", "javascript", "typeScript")
        return language.id.toLowerCase() in supportedLanguages
    }

    private fun getSupportedElementTypesForLanguage(language: Language): List<String> {
        return when (language.id.toLowerCase()) {
            "java" -> listOf("CLASS", "METHOD")
            "python" -> listOf("FUNCTION_DECLARATION", "CLASS_DECLARATION")
            "javascript", "typescript" -> listOf("JS:FUNCTION_DECLARATION", "JS:ES6_CLASS", "JS:TYPESCRIPT_FUNCTION", "JS:TYPESCRIPT_CLASS")
            else -> emptyList()
        }
    }
}
