package dev.nouseeker.outbox.config

import dev.nouseeker.outbox.converter.KafkaKeyValueConverter
import dev.nouseeker.outbox.handler.impl.DefaultKafkaHandler
import dev.nouseeker.outbox.producer.impl.KafkaProducer
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import java.io.Serializable

@ConditionalOnClass(KafkaTemplate::class)
class KafkaOutboxConfiguration {

    @Bean
    fun <K : Serializable, V> kafkaProducer(
        kafkaTemplate: KafkaTemplate<K, V>
    ) = KafkaProducer(kafkaTemplate)

    @Bean
    fun <K : Serializable, V> defaultOutboxHandler(
        outboxProducer: KafkaProducer<K, V>,
        kafkaConverter: KafkaKeyValueConverter<K, V>
    ) = DefaultKafkaHandler(outboxProducer, kafkaConverter)

    @Bean
    fun <K : Serializable, V> kafkaKeyValueConverter(
        producerFactory: ProducerFactory<K, V>
    ) = KafkaKeyValueConverter(producerFactory)
}