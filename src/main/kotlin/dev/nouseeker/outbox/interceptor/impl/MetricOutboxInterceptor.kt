package dev.nouseeker.outbox.interceptor.impl

import dev.nouseeker.outbox.interceptor.OutboxInterceptor
import dev.nouseeker.outbox.meter.OutboxMeterService
import dev.nouseeker.outbox.model.MessageResult
import dev.nouseeker.outbox.model.OutboxMessage
import dev.nouseeker.outbox.utils.OUTBOX_MESSAGE_METRIC
import io.micrometer.core.instrument.Tag
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import java.util.concurrent.CompletableFuture

@Order(LOWEST_PRECEDENCE)
class MetricOutboxInterceptor(
    private val metricService: OutboxMeterService
) : OutboxInterceptor {

    private companion object {
        const val TOPIC_NAME_TAG = "topic"
        const val OBJECT_ID_TAG = "object_id"
        const val OBJECT_TYPE_TAG = "object_type"
    }

    override fun beforeCall(
        topicName: String,
        message: OutboxMessage
    ) {
        metricService.success(
            name = "${OUTBOX_MESSAGE_METRIC}_processing",
            tags = listOf(
                Tag.of(TOPIC_NAME_TAG, topicName),
                Tag.of(OBJECT_ID_TAG, message.objectId.toString()),
                Tag.of(OBJECT_TYPE_TAG, message.objectType),
            )
        )
    }

    override fun afterCall(
        topicName: String,
        message: OutboxMessage,
        callback: CompletableFuture<MessageResult>
    ) {
        callback.whenComplete { _, ex ->
            if (ex == null) {
                metricService.success(
                    name = "${OUTBOX_MESSAGE_METRIC}_processing_success",
                    tags = listOf(
                        Tag.of(TOPIC_NAME_TAG, topicName),
                        Tag.of(OBJECT_ID_TAG, message.objectId.toString()),
                        Tag.of(OBJECT_TYPE_TAG, message.objectType),
                    )
                )
            } else {
                onErrorMetric(topicName, message, ex)
            }
        }
    }

    override fun onError(
        topicName: String,
        message: OutboxMessage,
        ex: Exception
    ) = onErrorMetric(topicName, message, ex)

    private fun onErrorMetric(
        topicName: String,
        message: OutboxMessage,
        ex: Throwable?
    ) = metricService.error(
        name = "${OUTBOX_MESSAGE_METRIC}_processing_error",
        error = ex?.message,
        ex = ex,
        tags = listOf(
            Tag.of(TOPIC_NAME_TAG, topicName),
            Tag.of(OBJECT_ID_TAG, message.objectId.toString()),
            Tag.of(OBJECT_TYPE_TAG, message.objectType)
        )
    )
}