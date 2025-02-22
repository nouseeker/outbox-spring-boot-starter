package dev.nouseeker.outbox.meter

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import mu.KLogging
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class OutboxMeterService(
    private val registry: MeterRegistry
) {
    private companion object : KLogging() {
        private const val ERROR_TAG = "error"
        private const val EXCEPTION_TAG = "ex"
        private const val NONE_TAG_VALUE = "none"
    }

    fun summary(
        name: String,
        amount: Double,
        tags: List<Tag> = emptyList(),
    ) {
        registry.summary(
            name,
            tags
        ).record(amount)
    }

    fun success(
        name: String,
        tags: List<Tag> = emptyList()
    ) = registry.counter(
        name,
        tags
    ).increment()

    fun error(
        name: String,
        error: String?,
        ex: Throwable? = null,
        tags: List<Tag> = emptyList()
    ) = registry.counter(
        name,
        tags.plus(
            listOf(
                Tag.of(ERROR_TAG, error ?: NONE_TAG_VALUE),
                Tag.of(EXCEPTION_TAG, ex?.javaClass?.name ?: NONE_TAG_VALUE)
            )
        )
    ).increment()

    fun measure(
        name: String,
        additionalTagsF: () -> List<Tag> = { emptyList() },
        block: () -> Unit
    ) {
        val startedAt = LocalDateTime.now()

        try {
            block().also {
                val additionalTags = runCatching { additionalTagsF() }
                    .onFailure { logger.error("Fail on extract additional tags", it) }
                    .getOrDefault(emptyList())

                runCatching {
                    registry.timer(
                        name,
                        additionalTags.plus(
                            listOf(
                                Tag.of(ERROR_TAG, NONE_TAG_VALUE),
                                Tag.of(EXCEPTION_TAG, NONE_TAG_VALUE),
                            )
                        )
                    ).record(startedAt.until(LocalDateTime.now(), ChronoUnit.MILLIS), TimeUnit.MILLISECONDS)
                }
            }
        } catch (e: Throwable) {
            registry.timer(
                name,
                listOf(
                    Tag.of(ERROR_TAG, e.message.toString()),
                    Tag.of(EXCEPTION_TAG, e.javaClass.name),
                )
            ).record(startedAt.until(LocalDateTime.now(), ChronoUnit.MILLIS), TimeUnit.MILLISECONDS)
        }
    }
}