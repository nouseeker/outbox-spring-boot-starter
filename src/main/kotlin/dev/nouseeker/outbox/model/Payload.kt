package dev.nouseeker.outbox.model

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

sealed class Payload

data class KafkaPayload<K, V>(
    val producerRecord: ProducerRecord<K, V>,
    val recordMetadata: RecordMetadata
) : Payload()