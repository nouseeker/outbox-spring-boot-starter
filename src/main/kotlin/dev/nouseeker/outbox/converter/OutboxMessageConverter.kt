package dev.nouseeker.outbox.converter

import dev.nouseeker.outbox.model.OutboxMessage
import dev.nouseeker.outbox.model.OutboxStatus
import dev.nouseeker.outbox.repository.entity.OutboxMessageEntity

class OutboxMessageConverter {

    fun toModel(entity: OutboxMessageEntity) = OutboxMessage(
        id = entity.id,
        type = entity.type,
        status = OutboxStatus.valueOf(entity.status),
        objectId = entity.objectId,
        objectType = entity.objectType,
        payload = entity.payload,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    fun toEntity(model: OutboxMessage) = OutboxMessageEntity(
        id = model.id,
        type = model.type,
        status = model.status.name,
        objectId = model.objectId,
        objectType = model.objectType,
        payload = model.payload,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt
    )
}