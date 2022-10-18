package com.tabnine.inline

import com.tabnine.capabilities.CapabilitiesService
import com.tabnine.capabilities.Capability
import com.tabnine.userSettings.AppSettingsState

object DebounceUtils {
    private val DEBOUNCE_CAPABILITIES = arrayListOf(
        Capability.DEBOUNCE_VALUE_300,
        Capability.DEBOUNCE_VALUE_600
    )

    @JvmStatic
    fun getDebounceInterval(): Long {
        return getDebounceMsFromCapabilities() ?: AppSettingsState.instance.debounceTime
    }

    @JvmStatic
    fun getDebounceMsFromCapabilities(): Long? {
        if (CapabilitiesService.getInstance().isCapabilityEnabled(Capability.DEBOUNCE_VALUE_300)) {
            return 300
        }
        if (CapabilitiesService.getInstance().isCapabilityEnabled(Capability.DEBOUNCE_VALUE_600)) {
            return 600
        }
        return null
    }

    @JvmStatic
    fun isFixedDebounceConfigured(): Boolean {
        return DEBOUNCE_CAPABILITIES.stream().anyMatch(CapabilitiesService.getInstance()::isCapabilityEnabled)
    }
}
