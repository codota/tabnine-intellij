package com.tabnineCommon.general

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import java.util.Optional
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.function.Consumer
import java.util.function.Function

open class TopicBasedState<T, S : Consumer<T>>(
    private val topic: Topic<S>,
    @Volatile private var value: T? = null
) {
    private val rwLock: ReadWriteLock = ReentrantReadWriteLock()
    private val hasChanged = AtomicBoolean(false)

    open fun get() = rwLock.readLock().withLock {
        value
    }

    fun getOptional() = Optional.ofNullable(get())

    fun set(newValue: T) {
        rwLock.writeLock().withLock {
            hasChanged.set(value != newValue)
            value = newValue
        }

        notifyListeners()
    }

    private fun notifyListeners() {
        rwLock.readLock().withLock {
            if (hasChanged.getAndSet(false) && value != null) {
                ApplicationManager.getApplication().messageBus.syncPublisher(topic).accept(value!!)
            }
        }
    }

    fun set(accumulator: Function<T?, T>) {
        rwLock.writeLock().withLock {
            val newValue = accumulator.apply(value)

            hasChanged.set(value != newValue)
            value = newValue
        }

        notifyListeners()
    }

    fun onChange(parent: Disposable, subscription: S) {
        rwLock.readLock().withLock {
            if (value != null) {
                subscription.accept(value!!)
            }

            ApplicationManager.getApplication().messageBus.connect(parent)
                .subscribe(topic, subscription)
        }
    }

    fun onChange(subscription: S) {
        rwLock.readLock().withLock {
            if (value != null) {
                subscription.accept(value!!)
            }

            ApplicationManager.getApplication().messageBus.connect().subscribe(topic, subscription)
        }
    }
}

fun <T> Lock.withLock(toDo: () -> T): T {
    lock()
    try {
        return toDo()
    } finally {
        unlock()
    }
}
