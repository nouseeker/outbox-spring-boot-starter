package dev.nouseeker.outbox.repository

import dev.nouseeker.outbox.repository.entity.OutboxMessageEntity
import java.time.LocalDateTime

interface OutboxMessageRepository {

    fun create(entity: OutboxMessageEntity)

    fun findAllNew(limit: Int): List<OutboxMessageEntity>

    fun updateAll(entities: List<OutboxMessageEntity>)

    fun deleteAllOlderThan(date: LocalDateTime): Int
}