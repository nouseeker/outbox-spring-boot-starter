package dev.nouseeker.outbox.handler

import dev.nouseeker.outbox.model.MessageResult
import dev.nouseeker.outbox.model.OutboxMessage
import java.util.concurrent.CompletableFuture

interface OutboxHandler {

    fun handle(topicName: String, message: OutboxMessage): CompletableFuture<MessageResult>

    val type: String
}