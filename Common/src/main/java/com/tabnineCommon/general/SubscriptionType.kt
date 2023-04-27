package com.tabnineCommon.general

import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus
import java.util.EnumSet
import javax.swing.Icon

val PRO_SERVICE_LEVELS: Set<ServiceLevel> = EnumSet.of(ServiceLevel.PRO, ServiceLevel.TRIAL)

interface ISubscriptionType {
    fun getTabnineLogo(cloudConnectionHealthStatus: CloudConnectionHealthStatus): Icon
}

enum class SubscriptionType : ISubscriptionType {
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
            return if (cloudConnectionHealthStatus == CloudConnectionHealthStatus.Failed)
                StaticConfig.ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE
            else StaticConfig.ICON_AND_NAME_ENTERPRISE
        }
    };

    companion object {
        @JvmStatic
        fun getSubscriptionType(serviceLevel: ServiceLevel?): SubscriptionType {
            if (serviceLevel == ServiceLevel.TRIAL || PRO_SERVICE_LEVELS.contains(serviceLevel)) {
                return Pro
            }
            if (serviceLevel == ServiceLevel.BUSINESS) {
                return Enterprise
            }
            return Starter
        }
    }
}
