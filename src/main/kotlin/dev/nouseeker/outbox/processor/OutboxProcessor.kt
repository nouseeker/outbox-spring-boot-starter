package dev.nouseeker.outbox.processor

interface OutboxProcessor {

    fun processMessages()
}