package com.tabnineCommon.state

import com.intellij.openapi.components.service
import com.tabnineCommon.binary.requests.config.StateRequest
import com.tabnineCommon.general.IProviderOfThings
import com.tabnineCommon.general.ServiceLevel

class UserState private constructor() {
    val suggestionHintState: SuggestionHintState
    val serviceLevel: ServiceLevel?

    init {
        val binaryRequestFacade = service<IProviderOfThings>().binaryRequestFacade
        val stateResponse = binaryRequestFacade.executeRequest(StateRequest())
        suggestionHintState = SuggestionHintState(stateResponse?.installationTime)
        serviceLevel = stateResponse?.serviceLevel
    }

    companion object {
        var userState: UserState? = null

        @JvmStatic
        fun init() {
            if (userState == null) {
                userState = UserState()
            }
        }

        @JvmStatic
        val instance: UserState
            get() {
                init()
                return userState!!
            }
    }
}
