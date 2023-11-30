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
            ProviderInfo(lang, TabnineLensProvider(getSupportedElementTypesForLanguage(lang)))
        }
    }

    private fun shouldSupport(language: Language): Boolean {
        val supportedLanguages = setOf("java", "python", "javascript")
        return language.id.toLowerCase() in supportedLanguages
    }

    private fun getSupportedElementTypesForLanguage(language: Language): List<String> {
        return when (language.id.toLowerCase()) {
            "java" -> listOf("CLASS", "METHOD")
            "python" -> listOf("Py:CLASS_DECLARATION", "Py:FUNCTION_DECLARATION")
            "javascript" -> listOf("JS:FUNCTION_DECLARATION", "JS:ES6_CLASS", "JS:TYPESCRIPT_FUNCTION", "JS:TYPESCRIPT_CLASS")
            else -> emptyList()
        }
    }
}
