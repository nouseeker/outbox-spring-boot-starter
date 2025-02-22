package dev.nouseeker.outbox.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "outbox")
data class OutboxProperties(
    val topic: Map<String, String>,
    val message: OutboxMessageProperties
) {
    data class OutboxMessageProperties(
        val limit: Int = 500,
        val cleanDays: Long = 30
    )
}