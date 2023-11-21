package com.tabnineCommon.general

import com.intellij.util.messages.Topic
import java.util.function.Consumer

open class TopicBasedNonNullState<T, S : Consumer<T>>(
    topic: Topic<S>,
    initialValue: T
) : TopicBasedState<T, S>(topic, initialValue) {
    override fun get(): T {
        return super.get()!!
    }
}
