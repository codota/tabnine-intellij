package com.tabnineSelfHosted

import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus
import com.tabnineCommon.general.ISubscriptionType
import com.tabnineSelfHosted.general.StaticConfig
import javax.swing.Icon
import com.tabnineCommon.general.StaticConfig as CommonStaticConfig

enum class EnterpriseSubscriptionType : ISubscriptionType {
    Enterprise {
        override fun getTabnineLogo(cloudConnectionHealthStatus: CloudConnectionHealthStatus): Icon {
            if (cloudConnectionHealthStatus == CloudConnectionHealthStatus.Failed)
                return CommonStaticConfig.ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE

            if (StaticConfig.getTabnineEnterpriseHost() != null) {
                return CommonStaticConfig.ICON_AND_NAME_ENTERPRISE
            }
            return CommonStaticConfig.ICON_AND_NAME_CONNECTION_LOST_ENTERPRISE
        }
    };
}
