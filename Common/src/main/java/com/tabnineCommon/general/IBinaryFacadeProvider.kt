package com.tabnineCommon.general

import com.tabnineCommon.binary.BinaryRequestFacade

interface IBinaryFacadeProvider {
    fun getBinaryRequestFacade(serverUrl: String?): BinaryRequestFacade
}
