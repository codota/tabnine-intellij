package com.tabnine.userSnippets

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.tabnine.binary.requests.userSnippets.SaveSnippetRequest
import com.tabnine.binary.requests.userSnippets.SaveSnippetResponse
import com.tabnine.general.DependencyContainer
import com.tabnine.general.StaticConfig

class AddUserSnippetAction : AnAction() {
    private val binaryRequestFacade = DependencyContainer.instanceOfBinaryRequestFacade()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)

        if (!editor.selectionModel.hasSelection() || editor.selectionModel.selectedText == null) {
            showNotification("You must select some text to add a snippet.")
            return
        }
        val result = executeSaveSnippetRequest(editor)
        if (result == null) {
            showNotification("No response from Tabnine Engine")
            return
        }
        if (result.isSuccess()) {
            showNotification("Snippet saved successfully!")
            return
        }
        val error = result.asError()?.error!!
        showNotification("Failed to save snippet: $error")
    }

    private fun executeSaveSnippetRequest(editor: Editor): SaveSnippetResponse? {
        val filename = FileDocumentManager.getInstance().getFile(editor.document)?.path.orEmpty()
        val saveSnippetRequest = SaveSnippetRequest(
            editor.selectionModel.selectedText!!,
            filename,
            editor.selectionModel.selectionStart,
            editor.selectionModel.selectionEnd
        )

        return binaryRequestFacade.executeRequest(saveSnippetRequest)
    }

    private fun showNotification(text: String) {
        val notification = Notification(
            StaticConfig.BRAND_NAME,
            StaticConfig.NOTIFICATION_ICON, NotificationType.INFORMATION
        )
        notification.setContent(text)
        notification.addAction(object : AnAction("Ok") {
            override fun actionPerformed(e: AnActionEvent) {
                notification.expire()
            }
        })

        Notifications.Bus.notify(notification)
    }
}
