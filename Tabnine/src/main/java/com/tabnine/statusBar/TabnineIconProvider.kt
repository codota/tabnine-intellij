package com.tabnine.statusBar

import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.general.ServiceLevel
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.general.getSubscriptionType
import javax.swing.Icon

class TabnineIconProvider {
    companion object {
        fun getIcon(
            serviceLevel: ServiceLevel?,
            isLoggedIn: Boolean?,
            cloudConnectionHealthStatus: CloudConnectionHealthStatus
        ): Icon {
            if (!CapabilitiesService.getInstance().isReady || isLoggedIn == null || serviceLevel == null || (
                CapabilitiesService.getInstance()
                    .isCapabilityEnabled(Capability.FORCE_REGISTRATION) &&
                    !isLoggedIn && serviceLevel == ServiceLevel.FREE
                )
            ) {
                return StaticConfig.ICON_AND_NAME
            }

            return getSubscriptionType(serviceLevel).getTabnineLogo(cloudConnectionHealthStatus)
        }
    }
}
