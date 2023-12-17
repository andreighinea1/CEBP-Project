package com.cebp_project.messenger.constants;

/**
 * Constants holds various configuration constants used throughout the messaging application.
 * This record centralizes settings related to timeouts, queue sizes, and RabbitMQ configuration.
 */
public record Constants() {
    public static final long TopicOrchestratorMaxTimeout = 5000; // Maximum timeout for topic messages in milliseconds
    public static final int MessageQueueMaxSize = 1000;          // Maximum size of the message queue
    public static final String RABBITMQ_BROADCAST_QUEUE_NAME = "broadcast_queue"; // Name of the RabbitMQ broadcast queue
    public static final String RABBITMQ_TOPIC_QUEUE_NAME = "topic_queue";         // Name of the RabbitMQ topic queue
}
