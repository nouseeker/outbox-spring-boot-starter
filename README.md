# Spring Boot Starter implementing the Transactional Outbox Pattern

## Quick Start

1. Add the dependency to `build.gradle.kts`:
   ```kotlin
    implementation("dev.nouseeker:outbox-spring-boot-starter:$outboxVersion")
   ```
2. Add the `EnableTransactionOutbox` annotation at the application startup:
   ```kotlin
   @EnableTransactionOutbox
   @SpringBootApplication
   class Application
   
   fun main(args: Array<String>) {
       runApplication<Application>(*args)
   }
   ```

## Configuration

| Property                           | Type    | Default     | Description                                                                                             |
|------------------------------------|---------|-------------|---------------------------------------------------------------------------------------------------------|
| `outbox.topic.default`             | String  |             | Default topic where messages will be sent                                                               |
| `outbox.message.limit`             | Long    | 500         | Number of messages that can be sent at once                                                             |
| `outbox.message.clean-days`        | Long    | 30          | Cleanup of records in the table that were modified later than the current time minus the specified days |
| `outbox.scheduler.sender.enabled`  | Boolean | false       | Enable/disable the message sending scheduler                                                            |
| `outbox.scheduler.sender.delay`    | String  | 10000       | Fixed time (in milliseconds) for sending messages                                                       |
| `outbox.scheduler.cleaner.enabled` | String  | false       | Enable/disable the message cleanup scheduler                                                            |
| `outbox.scheduler.cleaner.cron`    | String  | 1 * * * * * | Fixed time (cron) for deleting old records from the table                                               |

### Example

```yaml
outbox:
  topic:
    default: default.v1.topic
    reply: reply.v1.topic
  message:
    limit: 500
    clean-days: 30
  scheduler:
    sender:
      enabled: true
      delay: 10000
    cleaner:
      enabled: true
      cron: "1 * * * * *"
```

## Extending Functionality

### Sending to different topics

To add a new topic, you need to:

1. Add a property to the configuration, where the key specifies the message type and the value is the topic name to send
   to:
   ```yaml
   outbox:
      topic:
         reply: reply.v1.topic
   ```
   where:
    - `reply` - the message type specified in the `outbox_messages.type` table. To set a custom handler for this message
      type, [see the handlers section](#adding-a-handler)
    - `reply.v1.topic` - the topic name
2. When saving a message to the `outbox_messages` table, specify the message type as defined in the configuration.

### Adding an Interceptor

1. Implement the methods of the `OutboxInterceptor` interface:
   ```kotlin
   class LoggingOutboxInterceptor : OutboxInterceptor {
   
      private companion object : KLogging()
   
      override fun beforeCall(
         topicName: String,
         message: OutboxMessage
      ) {
         logger.info { "before handling message with object id ${message.objectId} and object type ${message.objectType}" }
      }
   
      override fun afterCall(
         topicName: String,
         message: OutboxMessage,
         callback: CompletableFuture<MessageResult>
      ) {
         logger.info { "after handling message with object id ${message.objectId} and object type ${message.objectType}" }
      }
   
      override fun onError(
         topicName: String,
         message: OutboxMessage,
         ex: Exception
      ) {
         logger.error { "error handling message with object id ${message.objectId} and object type ${message.objectType}" }
      }
   }
   ```
2. Add a bean in the configuration class:
   ```kotlin
   @Bean
   fun loggingOutboxListener() = LoggingOutboxListener()
   ```

### Adding a Handler

1. Implement the methods of the `OutboxHandler` interface, where the
   `getType` method determines the topic type to be processed ([see](#sending-to-different-topics)).
2. Add a bean in the configuration class.

> :warning: If a suitable handler cannot be found during message processing, the default handler will be used.

## Metrics

| Metric                              | Type    | Available Tags                           | Description                               |
|-------------------------------------|---------|------------------------------------------|-------------------------------------------|
| `outbox_message_processing`         | counter | object_id, object_type, topic            | Total number of processed messages        |
| `outbox_message_processing_success` | counter | object_id, object_type, topic            | Number of successfully processed messages |
| `outbox_message_processing_error`   | counter | object_id, object_type, topic, error, ex | Number of messages processed with errors  |
| `outbox_message_processing_time`    | timer   | message_size                             | Processing time for all messages          |
| `outbox_message_deleted`            | summary |                                          | Aggregated number of deleted messages     |

## TODOs

- [ ] Change schedulers to use ShedLock
- [ ] Implement a repository for outbox messages in the client code