package dev.nouseeker.outbox.config

import dev.nouseeker.outbox.config.properties.OutboxProperties
import dev.nouseeker.outbox.converter.OutboxMessageConverter
import dev.nouseeker.outbox.handler.OutboxHandler
import dev.nouseeker.outbox.interceptor.OutboxInterceptor
import dev.nouseeker.outbox.interceptor.OutboxInterceptorFacade
import dev.nouseeker.outbox.meter.OutboxMeterService
import dev.nouseeker.outbox.processor.OutboxProcessor
import dev.nouseeker.outbox.processor.impl.OutboxProcessorImpl
import dev.nouseeker.outbox.repository.OutboxMessageRepository
import dev.nouseeker.outbox.repository.impl.JdbcOutboxMessageRepository
import dev.nouseeker.outbox.scheduler.OutboxCleanerScheduler
import dev.nouseeker.outbox.scheduler.OutboxSenderScheduler
import dev.nouseeker.outbox.service.OutboxMessageService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@AutoConfiguration
@EnableConfigurationProperties(OutboxProperties::class)
@Import(KafkaOutboxConfiguration::class, MetricOutboxConfiguration::class)
class OutboxAutoConfiguration {

    @Bean
    fun jdbcOutboxRepository(
        jdbcTemplate: JdbcTemplate
    ) = JdbcOutboxMessageRepository(jdbcTemplate)

    @Bean
    fun outboxService(
        outboxConverter: OutboxMessageConverter,
        outboxRepository: OutboxMessageRepository
    ) = OutboxMessageService(outboxRepository, outboxConverter)

    @Bean
    fun outboxConverter() = OutboxMessageConverter()

    @Bean
    fun outboxProcessor(
        handlers: List<OutboxHandler>,
        properties: OutboxProperties,
        outboxInterceptorFacade: OutboxInterceptorFacade,
        outboxService: OutboxMessageService,
        outboxMeterService: OutboxMeterService,
    ) = OutboxProcessorImpl(
        handlers,
        properties,
        outboxInterceptorFacade,
        outboxService,
        outboxMeterService,
    )

    @Bean
    fun outboxCleanerScheduler(
        properties: OutboxProperties,
        service: OutboxMessageService,
        meterService: OutboxMeterService
    ) = OutboxCleanerScheduler(properties, service, meterService)

    @Bean
    fun outboxSenderScheduler(
        processor: OutboxProcessor,
    ) = OutboxSenderScheduler(processor)

    @Bean
    fun outboxListenerFacade(
        outboxInterceptors: List<OutboxInterceptor>
    ) = OutboxInterceptorFacade(outboxInterceptors)
}