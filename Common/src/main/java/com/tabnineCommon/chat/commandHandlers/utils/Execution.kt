package com.tabnineCommon.chat.commandHandlers.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.CompletableFuture

fun <T> submitReadAction(action: () -> T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    AppExecutorUtil.getAppExecutorService().submit {
        ApplicationManager.getApplication().runReadAction {
            try {
                val result = ReadAction.compute<T, Throwable>(action)
                future.complete(result)
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }
    }
    return future
}
