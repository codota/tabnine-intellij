package com.tabnineCommon.state

import com.tabnine.general.DependencyContainer
import com.tabnineCommon.binary.requests.config.StateRequest
import com.tabnineCommon.general.ServiceLevel

class UserState private constructor() {
    val suggestionHintState: SuggestionHintState
    val serviceLevel: ServiceLevel?

    init {
        val binaryRequestFacade = com.tabnine.general.DependencyContainer.instanceOfBinaryRequestFacade()
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
