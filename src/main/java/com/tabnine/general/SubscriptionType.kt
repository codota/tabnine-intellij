package com.tabnine.general

import com.tabnine.binary.requests.config.CloudConnectionHealthStatus
import java.util.EnumSet
import javax.swing.Icon

val PRO_SERVICE_LEVELS: Set<ServiceLevel> = EnumSet.of(ServiceLevel.PRO, ServiceLevel.TRIAL)

enum class SubscriptionType {
    Starter {
        override fun getTabnineLogo(cloudConnectionHealthStatus: CloudConnectionHealthStatus): Icon {
            return if (cloudConnectionHealthStatus === CloudConnectionHealthStatus.Ok)
                StaticConfig.ICON_AND_NAME_STARTER;
            else StaticConfig.ICON_AND_NAME_CONNECTION_LOST_STARTER;
        }
    },
    Pro {
        override fun getTabnineLogo(cloudConnectionHealthStatus: CloudConnectionHealthStatus): Icon {
            return if (cloudConnectionHealthStatus === CloudConnectionHealthStatus.Ok)
                StaticConfig.ICON_AND_NAME_PRO;
            else StaticConfig.ICON_AND_NAME_CONNECTION_LOST_PRO;
        }
    },
    Enterprise {
        override fun getTabnineLogo(cloudConnectionHealthStatus: CloudConnectionHealthStatus): Icon {
            val hasCloud2UrlConfigured =
                StaticConfig.getTabnineEnterpriseHost()?.filter { it.isNotEmpty() }?.isPresent ?: false

            if (hasCloud2UrlConfigured && cloudConnectionHealthStatus === CloudConnectionHealthStatus.Ok) {
                return StaticConfig.ICON_AND_NAME_ENTERPRISE
            }

            return StaticConfig.ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE;
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
