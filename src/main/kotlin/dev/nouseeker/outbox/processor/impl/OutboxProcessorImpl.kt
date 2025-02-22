package dev.nouseeker.outbox.processor.impl

import dev.nouseeker.outbox.config.properties.OutboxProperties
import dev.nouseeker.outbox.handler.OutboxHandler
import dev.nouseeker.outbox.interceptor.OutboxInterceptorFacade
import dev.nouseeker.outbox.meter.OutboxMeterService
import dev.nouseeker.outbox.model.OutboxMessage
import dev.nouseeker.outbox.model.OutboxStatus
import dev.nouseeker.outbox.processor.AbstractOutboxProcessor
import dev.nouseeker.outbox.service.OutboxMessageService
import dev.nouseeker.outbox.utils.OUTBOX_MESSAGE_METRIC
import io.micrometer.core.instrument.Tag
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

open class OutboxProcessorImpl(
    handlers: List<OutboxHandler>,
    properties: OutboxProperties,
    outboxInterceptorFacade: OutboxInterceptorFacade,
    private val outboxService: OutboxMessageService,
    private val meterService: OutboxMeterService,
) : AbstractOutboxProcessor(handlers, properties, outboxInterceptorFacade) {

    override fun processMessages() {
        logger.debug("Start of processing outbox messages")

        do {
            val messages = outboxService.findAllWaiting(properties.message.limit)

            meterService.measure(
                "${OUTBOX_MESSAGE_METRIC}_processing_time",
                additionalTagsF = {
                    listOf(
                        Tag.of("message_size", messages.size.toString())
                    )
                }
            ) {
                val sentMessages = mutableListOf<OutboxMessage>()
                val callbacks = messages.map { message ->
                    val handler = handlersMap[message.type] ?: defaultHandler
                    val topicName = properties.topic[message.type] ?: defaultTopic

                    val callback = processMessage(topicName, message, handler)
                    callback.whenComplete { _, ex ->
                        if (ex == null) {
                            sentMessages += message.copy(
                                status = OutboxStatus.DONE,
                                updatedAt = LocalDateTime.now()
                            )
                        } else {
                            logger.error(ex) { "Error occurred while processing message id: ${message.id} to topic: $topicName with handler: ${handler.javaClass.name}" }
                        }
                    }
                }
                CompletableFuture.allOf(*callbacks.toTypedArray()).join()
                outboxService.updateAll(sentMessages)
            }
        } while (messages.isNotEmpty())

        logger.debug("End of processing outbox messages")
    }
}