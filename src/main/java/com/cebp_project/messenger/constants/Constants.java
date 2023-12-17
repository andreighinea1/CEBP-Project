package com.cebp_project.messenger.constants;

public record Constants() {
    public static final long TopicOrchestratorMaxTimeout = 5000;
    public static final int MessageQueueMaxSize = 1000;
    public static final String RABBITMQ_BROADCAST_QUEUE_NAME = "broadcast_queue";
    public static final String RABBITMQ_TOPIC_QUEUE_NAME = "topic_queue";
}
