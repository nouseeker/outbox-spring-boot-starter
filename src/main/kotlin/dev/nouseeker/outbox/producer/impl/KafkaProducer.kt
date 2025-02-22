package dev.nouseeker.outbox.producer.impl

import dev.nouseeker.outbox.producer.MessageProducer
import mu.KLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.io.Serializable
import java.util.concurrent.CompletableFuture

class KafkaProducer<K : Serializable, V>(
    private val kafkaTemplate: KafkaTemplate<K, V>,
) : MessageProducer<K, V, SendResult<K, V>> {
    private companion object : KLogging()

    override fun send(topicName: String, key: K, message: V): CompletableFuture<SendResult<K, V>> = try {
        logger.info { "Sending message: $message with key: $key to topic: $topicName" }

        kafkaTemplate.send(topicName, key, message)
    } catch (ex: Exception) {
        logger.error { "Error on kafka producer with key: $key, message: $message and exception: ${ex.message}" }
        throw ex
    }

    override fun send(topicName: String, message: V): CompletableFuture<SendResult<K, V>> = try {
        logger.info { "Sending message: $message to topic: $topicName" }

        kafkaTemplate.send(topicName, message)
    } catch (ex: Exception) {
        logger.error { "Error on kafka producer message: $message and exception: ${ex.message}" }
        throw ex
    }
}