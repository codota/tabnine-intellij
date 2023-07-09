package com.tabnine.statusBar

import com.tabnineCommon.binary.requests.config.CloudConnectionHealthStatus
import com.tabnineCommon.general.ServiceLevel
import com.tabnineCommon.general.StaticConfig
import com.tabnineCommon.general.getSubscriptionType
import javax.swing.Icon

object TabnineIconProvider {
    @JvmStatic
    fun getIcon(
        serviceLevel: ServiceLevel?,
        isLoggedIn: Boolean?,
        cloudConnectionHealthStatus: CloudConnectionHealthStatus,
        isForcedRegistration: Boolean?
    ): Icon {
        if (isForcedRegistration == null || isLoggedIn == null || serviceLevel == null || (
            isForcedRegistration &&
                !isLoggedIn
            )
        ) {
            return StaticConfig.ICON_AND_NAME
        }

        return getSubscriptionType(serviceLevel).getTabnineLogo(cloudConnectionHealthStatus)
    }
}
