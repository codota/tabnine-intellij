package com.tabnine.lifecycle.pushtosignin

import com.intellij.util.messages.Topic
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.general.TopicBasedNonNullState
import com.tabnineCommon.lifecycle.BinaryStateSingleton
import com.tabnineCommon.lifecycle.CapabilitiesStateSingleton
import com.tabnineCommon.lifecycle.PluginInstalled
import java.util.function.Consumer

class PushToSignInState :
    TopicBasedNonNullState<PushToSignInStateData, PushToSignInState.OnChange>(
        Topic.create("PushToSignInState", OnChange::class.java),
        PushToSignInStateData()
    ) {
    public fun interface OnChange : Consumer<PushToSignInStateData>

    init {
        CapabilitiesStateSingleton.instance.onChange { c ->
            val isForceRegistration = c.isEnabled(Capability.FORCE_REGISTRATION)

            setWithNonNull {
                it.copy(isForceRegistration = isForceRegistration)
            }
        }

        PluginInstalled.subscribe {
            setWithNonNull { s ->
                s.copy(isNewInstallation = it)
            }
        }

        BinaryStateSingleton.instance.onChange { state ->
            setWithNonNull {
                it.copy(isLoggedIn = state.isLoggedIn)
            }
        }
    }
}

data class PushToSignInStateData(
    val isLoggedIn: Boolean? = BinaryStateSingleton.instance.get()?.isLoggedIn,
    val isNewInstallation: Boolean? = PluginInstalled.isNewInstallation,
    val isForceRegistration: Boolean = CapabilitiesService.getInstance()
        .isCapabilityEnabled(Capability.FORCE_REGISTRATION)
)
