package dev.nouseeker.outbox.repository.entity

import java.time.LocalDateTime
import java.util.*

open class OutboxMessageEntity(
    val id: UUID,
    val type: String,
    val status: String,
    val objectId: Long,
    val objectType: String,
    val payload: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)