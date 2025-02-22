package dev.nouseeker.outbox.model

import java.time.LocalDateTime
import java.util.*

data class OutboxMessage(
    val id: UUID,
    val type: String,
    val status: OutboxStatus,
    val objectId: Long,
    val objectType: String,
    val payload: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)