package com.tabnineCommon.general

import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.config.Config
import java.util.EnumSet
import javax.swing.Icon

val PRO_SERVICE_LEVELS: Set<ServiceLevel> = EnumSet.of(ServiceLevel.PRO, ServiceLevel.TRIAL)

enum class SubscriptionType {
    Starter {
        override fun getTabnineLogo(cloudConnectionHealthStatus: CloudConnectionHealthStatus): Icon {
            return if (cloudConnectionHealthStatus == CloudConnectionHealthStatus.Ok &&
                !CapabilitiesService.getInstance().isCapabilityEnabled(Capability.FORCE_REGISTRATION)
            )
                StaticConfig.ICON_AND_NAME_STARTER;
            else StaticConfig.ICON_AND_NAME_CONNECTION_LOST_STARTER;
        }
    },
    Pro {
        override fun getTabnineLogo(cloudConnectionHealthStatus: CloudConnectionHealthStatus): Icon {
            return if (cloudConnectionHealthStatus == CloudConnectionHealthStatus.Ok)
                StaticConfig.ICON_AND_NAME_PRO;
            else StaticConfig.ICON_AND_NAME_CONNECTION_LOST_PRO;
        }
    },
    Enterprise {
        override fun getTabnineLogo(cloudConnectionHealthStatus: CloudConnectionHealthStatus): Icon {
            if (cloudConnectionHealthStatus == CloudConnectionHealthStatus.Failed) {
                return StaticConfig.ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE
            }

            if (!Config.IS_SELF_HOSTED) {
                return StaticConfig.ICON_AND_NAME_ENTERPRISE
            }

            val hasCloud2UrlConfigured =
                StaticConfig.getTabnineEnterpriseHost()?.filter { it.isNotEmpty() }?.isPresent ?: false

            if (hasCloud2UrlConfigured) {
                return StaticConfig.ICON_AND_NAME_ENTERPRISE
            }
            return StaticConfig.ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE
        }
    };

    abstract fun getTabnineLogo(cloudConnectionHealthStatus: CloudConnectionHealthStatus): Icon
}

fun getSubscriptionType(serviceLevel: ServiceLevel?): SubscriptionType {
    if (serviceLevel == ServiceLevel.TRIAL || PRO_SERVICE_LEVELS.contains(serviceLevel)) {
        return SubscriptionType.Pro
    }
    if (serviceLevel == ServiceLevel.BUSINESS) {
        return SubscriptionType.Enterprise
    }
    return SubscriptionType.Starter
}
