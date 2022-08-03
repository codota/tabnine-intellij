package com.tabnine.state

import com.tabnine.binary.requests.config.StateRequest
import com.tabnine.general.DependencyContainer

class UserState private constructor() {
    val suggestionHintState: SuggestionHintState

    init {
        val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()
        val stateResponse = binaryRequestFacade.executeRequest(StateRequest())
        suggestionHintState = SuggestionHintState(stateResponse?.installationTime)
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
        val instance: UserState?
            get() {
                init()
                return userState
            }
    }
}
