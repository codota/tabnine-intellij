package com.tabnineSelfHosted.dialogs

import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.ui.Messages

object Dialogs {
    @JvmStatic
    fun showRestartDialog(message: String) {
        ServiceManager.invokeLater {
            val result = Messages.showYesNoDialog(
                message,
                "Tabnine",
                "Restart Now",
                "Restart Later",
                Messages.getInformationIcon()
            )

            if (result == Messages.YES) {
                (ServiceManager as ApplicationEx).restart(true)
            }
        }
    }
}
