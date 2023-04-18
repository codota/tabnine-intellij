package com.tabnineCommon.inline

import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.userSettings.AppSettingsState

object DebounceUtils {
    private val DEBOUNCE_CAPABILITIES = arrayListOf(
        Capability.DEBOUNCE_VALUE_300,
        Capability.DEBOUNCE_VALUE_600,
        Capability.DEBOUNCE_VALUE_900,
        Capability.DEBOUNCE_VALUE_1200,
        Capability.DEBOUNCE_VALUE_1500,
    )

    @JvmStatic
    fun getDebounceInterval(): Long {
        return getDebounceMsFromCapabilities() ?: AppSettingsState.instance.debounceTime
    }

    @JvmStatic
    fun getDebounceMsFromCapabilities(): Long? {
        if (isCapabilityEnabled(Capability.DEBOUNCE_VALUE_300)) return 300
        if (isCapabilityEnabled(Capability.DEBOUNCE_VALUE_600)) return 600
        if (isCapabilityEnabled(Capability.DEBOUNCE_VALUE_900)) return 900
        if (isCapabilityEnabled(Capability.DEBOUNCE_VALUE_1200)) return 1200
        if (isCapabilityEnabled(Capability.DEBOUNCE_VALUE_1500)) return 1500
        return null
    }

    @JvmStatic
    fun isFixedDebounceConfigured(): Boolean {
        return DEBOUNCE_CAPABILITIES.stream().anyMatch(CapabilitiesService.getInstance()::isCapabilityEnabled)
    }

    private fun isCapabilityEnabled(capability: Capability): Boolean {
        return CapabilitiesService.getInstance().isCapabilityEnabled(capability)
    }
}
