package com.tabnineCommon.chat.commandHandlers.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.CompletableFuture

enum class ActionPermissions {
    READ,
    WRITE
}

/**
 * Runs the given action *not* now - the thread in which the action
 * is executed depends on the permissions of the action.
 *
 * READ - runs on a thread provided by `AppExecutorUtil.getAppExecutorService().submit`,
 * with read permissions - i.e. acquires a read lock.
 *
 * WRITE - runs on the AWT thread, with write permissions.
 *
 * The result of the action is then captured in a CompletableFuture, and returned.
 */
class AsyncAction(private val permissions: ActionPermissions) {
    fun <T> execute(action: () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        val performAction: () -> Unit = {
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
                    performAction()
                }
            }

            ActionPermissions.WRITE -> ApplicationManager.getApplication().invokeLater {
                performAction()
            }
        }

        return future
    }
}
