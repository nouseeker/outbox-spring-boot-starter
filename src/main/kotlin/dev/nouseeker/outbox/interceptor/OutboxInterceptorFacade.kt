package dev.nouseeker.outbox.interceptor

import dev.nouseeker.outbox.model.MessageResult
import dev.nouseeker.outbox.model.OutboxMessage
import mu.KLogging
import java.util.concurrent.CompletableFuture

class OutboxInterceptorFacade(
    private val listeners: List<OutboxInterceptor>
) {
    private companion object : KLogging()

    fun beforeCall(
        topicName: String,
        message: OutboxMessage
    ) {
        logger.debug { "Start of processing listeners before handle outbox message: $message to topic: $topicName" }
        listeners.forEach { safeExecute(it.javaClass.name) { it.beforeCall(topicName, message) } }
        logger.debug { "End of processing listeners before handle outbox message: $message to topic: $topicName" }
    }

    fun afterCall(
        topicName: String,
        message: OutboxMessage,
        callback: CompletableFuture<MessageResult>
    ) {
        logger.debug { "Start of processing listeners after handle outbox message: $message to topic: $topicName" }
        listeners.forEach { safeExecute(it.javaClass.name) { it.afterCall(topicName, message, callback) } }
        logger.debug { "End of processing listeners after handle outbox message: $message to topic: $topicName" }
    }

    fun onError(
        topicName: String,
        message: OutboxMessage,
        ex: Exception
    ) {
        logger.debug { "Start of processing listeners on error handle outbox message: $message to topic: $topicName" }
        listeners.forEach { safeExecute(it.javaClass.name) { it.onError(topicName, message, ex) } }
        logger.debug { "End of processing listeners on error handle outbox message: $message to topic: $topicName" }
    }

    private inline fun safeExecute(className: String, block: () -> Unit) = runCatching {
        block()
    }.onFailure { logger.error("Listener $className threw an exception", it) }
}