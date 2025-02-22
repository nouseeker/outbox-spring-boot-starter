package dev.nouseeker.outbox.producer

import java.io.Serializable
import java.util.concurrent.CompletableFuture

interface MessageProducer<K : Serializable, V, R> {

    fun send(
        topicName: String,
        key: K,
        message: V
    ): CompletableFuture<R>

    fun send(
        topicName: String,
        message: V
    ): CompletableFuture<R>
}