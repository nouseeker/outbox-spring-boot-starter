package dev.nouseeker.outbox.converter

import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.core.ProducerFactory
import java.io.Serializable

open class KafkaKeyValueConverter<K : Serializable, V>(
    producerFactory: ProducerFactory<K, V>
) : KeyValueConverter<K, V> {
    private val keySerializer = producerFactory.keySerializer
        ?: throw IllegalStateException("Not found kafka key serializer")
    private val valueSerializer = producerFactory.valueSerializer
        ?: throw IllegalStateException("Not found kafka value serializer")

    override fun convertKey(key: Any): K = convert(keySerializer, key)

    override fun convertValue(value: Any): V = convert(valueSerializer, value)

    @Suppress("UNCHECKED_CAST")
    private fun <T> convert(serializer: Serializer<*>, obj: Any): T = when (serializer) {
        is StringSerializer -> obj.toString()
        is IntegerSerializer -> obj.toString().toInt()
        is LongSerializer -> obj.toString().toLong()
        else -> obj
    } as T
}