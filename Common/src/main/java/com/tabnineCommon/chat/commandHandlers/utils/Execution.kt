package com.tabnineCommon.chat.commandHandlers.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.CompletableFuture

enum class ActionPermissions {
    READ,
    WRITE
}

class AsyncAction(private val permissions: ActionPermissions) {
    fun <T> execute(action: () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        val completeAction: () -> Unit = {
            try {
                val result = action()
                future.complete(result)
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }

        when (permissions) {
            ActionPermissions.READ -> AppExecutorUtil.getAppExecutorService().submit {
                ApplicationManager.getApplication().runReadAction {
                    completeAction()
                }
            }

            ActionPermissions.WRITE -> ApplicationManager.getApplication().invokeLater {
                completeAction()
            }
        }

        return future
    }
}
