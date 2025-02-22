package dev.nouseeker.outbox.scheduler

import dev.nouseeker.outbox.processor.OutboxProcessor
import mu.KLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled

@ConditionalOnProperty(
    prefix = "outbox.scheduler.sender",
    name = ["enabled"],
    havingValue = "true"
)
class OutboxSenderScheduler(
    private val processor: OutboxProcessor,
) {
    private companion object : KLogging()

    @Scheduled(fixedDelayString = "\${outbox.scheduler.sender.delay:10000}")
    fun sendOutboxMessages() {
        logger.info("Start of sending outbox messages")

        processor.processMessages()

        logger.info("End of sending outbox messages")
    }
}