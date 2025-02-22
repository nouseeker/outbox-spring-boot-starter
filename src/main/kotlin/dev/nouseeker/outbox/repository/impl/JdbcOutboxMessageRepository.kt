package dev.nouseeker.outbox.repository.impl

import dev.nouseeker.outbox.repository.OutboxMessageRepository
import dev.nouseeker.outbox.repository.entity.OutboxMessageEntity
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

class JdbcOutboxMessageRepository(
    private val jdbcTemplate: JdbcTemplate
) : OutboxMessageRepository {

    private companion object {
        const val TABLE = "outbox_messages"
    }

    override fun create(entity: OutboxMessageEntity) {
        val sql = """
            INSERT INTO $TABLE (id, type, status, object_id, object_type, payload, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        jdbcTemplate.update(
            sql,
            entity.id.toString(),
            entity.type,
            entity.status,
            entity.objectId,
            entity.objectType,
            entity.payload,
            Timestamp.valueOf(entity.createdAt),
            Timestamp.valueOf(entity.updatedAt),
        )
    }

    override fun findAllNew(limit: Int): List<OutboxMessageEntity> {
        val sql = """
            SELECT * FROM $TABLE
            WHERE status = 'NEW'
            LIMIT ?
            FOR UPDATE SKIP LOCKED
        """.trimIndent()
        return jdbcTemplate.query(sql, mapper, limit)
    }

    override fun updateAll(entities: List<OutboxMessageEntity>) {
        val sql = """
            UPDATE $TABLE 
            SET type = ?,
                status = ?, 
                object_id = ?,
                object_type = ?,
                payload = ?,
                created_at = ?, 
                updated_at = ?,
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.batchUpdate(sql, object : BatchPreparedStatementSetter {
            override fun setValues(ps: PreparedStatement, idx: Int) {
                val entity = entities[idx]
                ps.setString(1, entity.type)
                ps.setString(2, entity.status)
                ps.setLong(3, entity.objectId)
                ps.setString(4, entity.objectType)
                ps.setString(5, entity.payload)
                ps.setTimestamp(6, Timestamp.valueOf(entity.createdAt))
                ps.setTimestamp(7, Timestamp.valueOf(entity.updatedAt))
                ps.setString(8, entity.id.toString())
            }

            override fun getBatchSize(): Int = entities.size
        })
    }

    override fun deleteAllOlderThan(date: LocalDateTime): Int {
        val sql = "DELETE FROM $TABLE WHERE updated_at <= ?"
        return jdbcTemplate.update(sql, Timestamp.valueOf(date))
    }

    private val mapper = RowMapper<OutboxMessageEntity> { rs, rowNum ->
        OutboxMessageEntity(
            id = UUID.fromString(rs.getString("id")),
            type = rs.getString("type"),
            status = rs.getString("status"),
            objectId = rs.getLong("object_id"),
            objectType = rs.getString("object_type"),
            payload = rs.getString("payload"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime(),
        )
    }
}